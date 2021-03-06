/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.unit.deployment.structure.extensions;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.AbstractMockDeployment;
import org.jboss.weld.mock.MockBeanDeploymentArchive;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.test.Utils;
import org.jboss.weld.util.serviceProvider.PackageServiceLoaderFactory;
import org.jboss.weld.util.serviceProvider.ServiceLoaderFactory;
import org.junit.Assert;
import org.junit.Test;

public class NonBdaExtensionTest
{
   /*
    * description = "WELD-233"
    */
   @Test
   public void test()
   {
      // Create the BDA in which we will deploy Observer1 and Foo. This is equivalent to a war or ejb jar
      final MockBeanDeploymentArchive bda1 = new MockBeanDeploymentArchive("1", Observer1.class, Foo.class);
      
      // Create the BDA to return from loadBeanDeploymentArchive for Observer2, this is probably a library, though could be another war or ejb jar
      // bda2 is accessible from bda1, but isn't added to it's accessibility graph by default. This similar to an archive which doesn't contain a beans.xml but does contain an extension 
      final BeanDeploymentArchive bda2 = new MockBeanDeploymentArchive("2", Observer2.class);
      
      // Create a deployment, that we can use to mirror the structure of one Extension inside a BDA, and one outside
      Deployment deployment = new AbstractMockDeployment(bda1)
      {
         
         public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass)
         {
            // Return bda2 if it is Observer2. Stick anything else which this test isn't about in bda1
            if (beanClass.equals(Observer2.class))
            {
               // If Observer2 is requested, then we need to add bda2 to the accessibility graph of bda1
               bda1.getBeanDeploymentArchives().add(bda2);
               return bda2;
            }
            else
            {
               return bda1;
            }
         }

      };
      
      TestContainer container = new TestContainer(new MockServletLifecycle(deployment, bda1));
      container.getLifecycle().initialize();
      
      // Add custom ServiceLoader so that we can load Extension services from current package, not META-INF/services
      // We do this after startContainer() so we replace the default impl
      deployment.getServices().add(ServiceLoaderFactory.class, new PackageServiceLoaderFactory(NonBdaExtensionTest.class.getPackage(), Extension.class));
      
      // Cause the container to deploy the beans etc.
      container.getLifecycle().beginApplication();
      
      // Get the bean manager for bda1 and bda2
      BeanManagerImpl beanManager1 = container.getBeanManager();
      BeanManagerImpl beanManager2 = container.getLifecycle().getBootstrap().getManager(bda2);
      
      Observer1 observer1 = Utils.getReference(beanManager1, Observer1.class);
      Assert.assertTrue(observer1.isBeforeBeanDiscoveryCalled());
      Assert.assertEquals(beanManager1, observer1.getBeforeBeanDiscoveryBeanManager());
      Assert.assertTrue(observer1.isAfterBeanDiscoveryCalled());
      Assert.assertTrue(observer1.isAfterDeploymentValidationCalled());
      Assert.assertTrue(observer1.isProcessInjectionTargetCalled());
      Assert.assertTrue(observer1.isProcessManagedBeanCalled());
      Assert.assertTrue(observer1.isProcessProducerCalled());
      
      Assert.assertEquals(1, beanManager2.getBeans(Observer2.class).size());
      // Also check that the accessibility graph has been updated
      Assert.assertEquals(1, beanManager1.getBeans(Observer2.class).size());
      
      Observer2 observer2 = Utils.getReference(beanManager2, Observer2.class);
      Assert.assertTrue(observer2.isBeforeBeanDiscoveryCalled());
      Assert.assertEquals(beanManager2, observer2.getBeforeBeanDiscoveryBeanManager());
      Assert.assertTrue(observer2.isAfterBeanDiscoveryCalled());
      Assert.assertTrue(observer2.isAfterDeploymentValidationCalled());
      Assert.assertTrue(observer2.isProcessInjectionTargetCalled());
      Assert.assertTrue(observer2.isProcessManagedBeanCalled());
      Assert.assertTrue(observer2.isProcessProducerCalled());
   }
   
   /*
    * description = "WELD-258"
    */
   @Test
   public void testEventsSentOnceOnly()
   {
   // Create the BDA in which we will deploy Observer1 and Foo. This is equivalent to a war or ejb jar
      final MockBeanDeploymentArchive bda1 = new MockBeanDeploymentArchive("1", CountingObserver1.class, Foo.class);
      
      // Create the BDA to return from loadBeanDeploymentArchive for Observer2, this is probably a library, though could be another war or ejb jar
      // bda2 is accessible from bda1, but isn't added to it's accessibility graph by default. This similar to an archive which doesn't contain a beans.xml but does contain an extension 
      final BeanDeploymentArchive bda2 = new MockBeanDeploymentArchive("2", CountingObserver2.class);
      
      // Create a deployment, that we can use to mirror the structure of one Extension inside a BDA, and one outside
      Deployment deployment = new AbstractMockDeployment(bda1)
      {
         
         public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass)
         {
            // Return bda2 if it is Observer2. Stick anything else which this test isn't about in bda1
            if (beanClass.equals(CountingObserver2.class) || beanClass.equals(Bar.class))
            {
               // If Observer2 is requested, then we need to add bda2 to the accessibility graph of bda1
               bda1.getBeanDeploymentArchives().add(bda2);
               return bda2;
            }
            else
            {
               return bda1;
            }
         }

      };
      
      TestContainer container = new TestContainer(new MockServletLifecycle(deployment, bda1));
      container.getLifecycle().initialize();
      
      // Add custom ServiceLoader so that we can load Extension services from current package, not META-INF/services
      // We do this after startContainer() so we replace the default impl
      deployment.getServices().add(ServiceLoaderFactory.class, new PackageServiceLoaderFactory(NonBdaExtensionTest.class.getPackage(), Extension.class));
      
      // Cause the container to deploy the beans etc.
      container.getLifecycle().beginApplication();
      
      // Get the bean manager for bda1 and bda2
      BeanManagerImpl beanManager1 = container.getBeanManager();
      
      CountingObserver1 observer1 = Utils.getReference(beanManager1, CountingObserver1.class);
      CountingObserver2 observer2 = Utils.getReference(beanManager1, CountingObserver2.class);
      Assert.assertEquals(1, observer1.getBeforeBeanDiscovery());
      Assert.assertEquals(1, observer1.getProcessFooManagedBean());
      Assert.assertEquals(0, observer1.getProcessBarManagedBean());
      Assert.assertEquals(1, observer2.getBeforeBeanDiscovery());
      Assert.assertEquals(1, observer2.getProcessFooManagedBean());
      Assert.assertEquals(1, observer2.getProcessBarManagedBean());
   }
   
}

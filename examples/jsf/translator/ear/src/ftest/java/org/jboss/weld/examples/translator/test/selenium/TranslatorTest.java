/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.examples.translator.test.selenium;

import static org.testng.Assert.assertTrue;

import org.jboss.test.selenium.AbstractTestCase;
import org.jboss.test.selenium.locator.XpathLocator;
import org.jboss.test.selenium.guard.request.RequestTypeGuardFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests translator example in Weld
 * 
 * @author mgencur
 * @author plenyi
 */
public class TranslatorTest extends AbstractTestCase
{
   protected String MAIN_PAGE = "/home.jsf";
   protected String INPUT_AREA = "id=TranslatorMain:text";
   protected String TRANSLATE_BUTTON = "id=TranslatorMain:button";
   protected String ONE_SENTENCE = "This is only one sentence.";
   protected String MORE_SENTENCES = "First sentence. Second and last sentence.";
   protected String ONE_SENTENCE_TRANSLATED = "Lorem ipsum dolor sit amet.";
   protected String MORE_SENTENCES_TRANSLATED = "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.";

   @Test
   public void translateTest()
   {
      browser.type(new XpathLocator(INPUT_AREA), ONE_SENTENCE);
      RequestTypeGuardFactory.waitHttp(selenium).click(new XpathLocator(TRANSLATE_BUTTON));
      assertTrue(browser.isTextPresent(ONE_SENTENCE_TRANSLATED), "One sentence translated into latin expected.");
      browser.type(new XpathLocator(INPUT_AREA), MORE_SENTENCES);
      RequestTypeGuardFactory.waitHttp(selenium).click(new XpathLocator(TRANSLATE_BUTTON));
      assertTrue(browser.isTextPresent(MORE_SENTENCES_TRANSLATED), "More sentences translated into latin expected.");      
   }
}

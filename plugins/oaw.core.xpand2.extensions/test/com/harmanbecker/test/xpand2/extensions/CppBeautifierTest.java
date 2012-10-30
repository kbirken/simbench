/*
 * Project: oaw.core.xpand2.extensions (c) copyright 2008 by Harman/Becker
 * Automotive Systems GmbH
 */
package com.harmanbecker.test.xpand2.extensions;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.xpand2.output.FileHandle;
import org.eclipse.xpand2.output.Outlet;
import org.openarchitectureware.xpand2.output.CppBeautifier;

/**
 * Tests the C++ Beautifier
 * 
 * @author DaWeber
 */
public class CppBeautifierTest extends TestCase
{
   protected StringBuffer input;
   protected StringBuffer output;
   protected File         targetFile;
   private CppBeautifier  testee;
   private FileHandle     fileHandle;

   protected void setUp() throws Exception
   {
      input = new StringBuffer();
      output = new StringBuffer();
      testee = new CppBeautifier();
      testee.setFileExtensions(".cpp");
      targetFile = new File("generatedSource.cpp");
      fileHandle = new FileHandle()
      {
         public CharSequence getBuffer()
         {
            return input.toString();
         }

         public String getFileEncoding()
         {
            return null;
         }

         public Outlet getOutlet()
         {
            return null;
         }

         public File getTargetFile()
         {
            return targetFile;
         }

         public boolean isAppend()
         {
            return false;
         }

         public boolean isOverwrite()
         {
            return false;
         }

         public void setBuffer(CharSequence arg0)
         {
            output.setLength(0);
            output.append(arg0);
         }

         public void writeAndClose()
         {
            // nothing to do
         }

         public String getAbsolutePath()
         {
            // TODO (kbirken@13.09.2010) Auto-generated method stub
            return null;
         }
      };
   }

   /**
    * @throws Exception if something goes wrong
    */
   public void testEmptyStringFormatting() throws Exception
   {
      assertEquals("", doFormat(""));
   }

   /**
    * @throws Exception if something goes wrong
    */
   public void testDefaultFormatting() throws Exception
   {
      assertEquals("class Foo\n" + "{\n" + "public:\n" + "   void bar();\n" + "};",
            doFormat("class Foo { public: void bar(); };"));
   }

   /**
    * @throws Exception if something goes wrong
    */
   public void testIgnoreExtensions() throws Exception
   {
      targetFile = new File("doc.xml");
      assertEquals("", doFormat("<xml><a/></xml>"));
   }

   /**
    * Small helper method to perform formatting.
    * 
    * @param code to be formatted
    * @return The code after it has been formatted
    */
   private String doFormat(String code)
   {
      input.setLength(0);
      input.append(code);
      testee.beforeWriteAndClose(fileHandle);
      return output.toString();
   }

}

/*
 * Contributors: Daniel Weber - Initial implementation
 */
package org.openarchitectureware.xpand2.output;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.formatter.CodeFormatterVisitor;
import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.mwe.core.resources.ResourceLoader;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.xpand2.output.FileHandle;
import org.eclipse.xpand2.output.JavaBeautifier;
import org.eclipse.xpand2.output.PostProcessor;

/**
 * An XPand post processor for C/C++ code formatting based on cdt's code
 * formatter.
 *
 * Is uses some internal cdt classes to do its job, but this was he only way to
 * get this to work outside an Eclipse runtime. Use it like this (from an oaw
 * workflow):
 *
 * <pre>
 *    &lt;beautifier class=&quot;oaw.tests.workflow.CppBeautifier&quot; configFile=&quot;myCFormatterOptions.xml&quot;/&gt;
 * </pre>
 *
 * In order to create a configuration file, use cdt's preferences to set
 * formatter options and then export that configuration to a file.
 *
 * @author DaWeber@harmanbecker.com
 */
public class CppBeautifier implements PostProcessor
{
   private static final String                              DEFAULT_CDT_OPTIONS = "org/openarchitectureware/xpand2/output/cdtformat-hb.xml";
   private String                                           configFile          = DEFAULT_CDT_OPTIONS;
   private String                                           fileExtensions          = ".cpp,.c,.hpp,.h,.cxx";

   private final HashMap<String, WeakReference<Properties>> settingsCache       = new HashMap<String, WeakReference<Properties>>();

   /** Logger instance. */
   private final Log                                        log                 = LogFactory
                                                                                      .getLog(getClass());

   /*
    * (non-Javadoc)
    *
    * @seeorg.openarchitectureware.xpand2.output.PostProcessor#afterClose(org.
    * openarchitectureware.xpand2.output.FileHandle)
    */
   public void afterClose(final org.eclipse.xpand2.output.FileHandle impl)
   {
      // nothing more to do
   }

   /*
    * The implementation could have been much easier:
    *
    * <pre> CodeFormatter formatter =
    * ToolFactory.createDefaultCodeFormatter(getFormatterOptions()); //
    * CodeFormatter formatter = ToolFactory.createDefaultCodeFormatter(null);
    * String SOURCE = impl.getBuffer().toString(); IDocument document = new
    * Document(SOURCE); try { formatter
    * .format(CodeFormatter.K_COMPILATION_UNIT, SOURCE, 0, SOURCE.length(), 0,
    * &quot;\n&quot;) .apply(document); impl.setBuffer(new
    * StringBuffer(document.get())); } catch(MalformedTreeException e) {
    * log.error(e.getMessage(), e); } catch(BadLocationException e) {
    * log.error(e.getMessage(), e); } </pre>
    *
    * but unfortunately, this requires the cdt plug-in to be up and running in
    * an Eclipse runtime, which does not work when running oaw workflows in a
    * standalone environment.
    *
    * @see
    * org.openarchitectureware.xpand2.output.PostProcessor#beforeWriteAndClose
    * (org.openarchitectureware.xpand2.output.FileHandle)
    */
   @SuppressWarnings("unchecked")
   public void beforeWriteAndClose(final FileHandle fileHandle)
   {
      if(iCareAbout(fileHandle.getTargetFile().getName()))
      {
         Map options = createFormatterOptions();
         String source = fileHandle.getBuffer().toString();
         try
         {
            CodeFormatterVisitor codeFormatter = new CodeFormatterVisitor(
                  createCodeFormatterSettings(options), options, 0, source.length());
            IASTTranslationUnit ast = createTranslationUnit(options, source);
            IDocument document = new Document(source);
            codeFormatter.format(source, ast).apply(document);
            fileHandle.setBuffer(new StringBuffer(document.get()));
         }
         catch(Exception e)
         {
            log.error(e.getMessage(), e);
         }
      }
   }

   /**
    * @param name
    * @return
    */
   private boolean iCareAbout(String name)
   {
      if(fileExtensions == null || fileExtensions.length() == 0)
      {
         return false;
      }
      StringTokenizer tokenizer = new StringTokenizer(fileExtensions, ",");
      while(tokenizer.hasMoreTokens())
      {
         if(name.endsWith(tokenizer.nextToken()))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * @return the configuration file for the formatter
    */
   public String getConfigFile()
   {
      return configFile;
   }

   /**
    * @param configFile configuration file for the formatter
    */
   public void setConfigFile(final String configFile)
   {
      this.configFile = configFile;
   }

   /**
    * @param options
    * @return
    */
   @SuppressWarnings("unchecked")
   private DefaultCodeFormatterOptions createCodeFormatterSettings(final Map options)
   {
      DefaultCodeFormatterOptions defaultSettings = DefaultCodeFormatterOptions
            .getDefaultSettings();
      defaultSettings.set(options);
      defaultSettings.line_separator = "\n";
      return defaultSettings;
   }

   /**
    * @return A map of formatter options. Either loaded from a given
    *         configuration file, or using built-in default values.
    */
   @SuppressWarnings("unchecked")
   private Map createFormatterOptions()
   {
      return readConfig(getConfigFile());
   }

   /**
    * @param options
    * @param source
    * @return
    * @throws CoreException
    */
   @SuppressWarnings("unchecked")
   private IASTTranslationUnit createTranslationUnit(final Map options,
         final String source) throws CoreException
   {
      return getLanguage(options).getASTTranslationUnit(
            new CodeReader(source.toCharArray()), new ScannerInfo(), null, null,
            ParserUtil.getParserLogService());
   }

   /**
    * Determines the language to be used based on the given options.
    *
    * @param options cdt formatter options
    * @return a suitable ILanguage. GPPLanguage as default.
    */
   @SuppressWarnings("unchecked")
   private ILanguage getLanguage(final Map options)
   {
      ILanguage language = (ILanguage)options
            .get(DefaultCodeFormatterConstants.FORMATTER_LANGUAGE);
      if(language == null)
      {
         language = GPPLanguage.getDefault();
      }
      return language;
   }

   /**
    * Return a Java Properties file representing the options that are in the
    * specified configuration file. In order to use this, simply export a
    * formatter configuration to a file.
    *
    * This code is mainly copied from {@link JavaBeautifier}, it might make
    * sense to factor this out to somewhere else so that more formatters can
    * make use of this.
    *
    * @todo replace this copy'n'pasted code
    *
    * @see JavaBeautifier
    */
   private Properties readConfig(final String filename)
   {
      if(settingsCache.containsKey(filename))
      {
         Properties cachedValue = settingsCache.get(filename).get();
         if(cachedValue != null)
         {
            return cachedValue;
         }
         settingsCache.remove(filename);
      }
      BufferedInputStream stream = null;
      BufferedReader reader = null;

      try
      {
         stream = new BufferedInputStream(loadFile(filename));
         final Properties formatterOptions = new Properties();
         if(filename.endsWith(".xml"))
         {
            reader = new BufferedReader(new InputStreamReader(stream));
            Pattern pattern = Pattern
                  .compile("<setting id=\"([^\"]*)\" value=\"([^\"]*)\"\\/>");
            for(String line = reader.readLine(); line != null; line = reader.readLine())
            {
               Matcher matcher = pattern.matcher(line);
               if(matcher.matches())
               {
                  formatterOptions.put(matcher.group(1), matcher.group(2));
               }
            }
         }
         else
         {
            formatterOptions.load(stream);
         }
         settingsCache.put(filename, new WeakReference<Properties>(formatterOptions));
         return formatterOptions;
      }
      catch(IOException e)
      {
         log.warn("Problem reading code formatter config file (" + e.getMessage() + ").");
      }
      finally
      {
         if(stream != null)
         {
            try
            {
               stream.close();
            }
            catch(IOException e)
            {
               /* ignore */
            }
         }
         if(reader != null)
         {
            try
            {
               reader.close();
            }
            catch(IOException e)
            {
               /* ignore */
            }
         }
      }
      return null;
   }

   /**
    * This is also stolen from {@link JavaBeautifier}, as it is required by
    * readConfig.
    *
    * @todo replace this copy'n'pasted code
    *
    * @param filename Path to a formatter configuration file
    * @return a corresponding File instance if the file can be located
    * @throws IOException If the file could not be found
    */
   private InputStream loadFile(final String filename) throws IOException
   {
      ResourceLoader resourceLoader = ResourceLoaderFactory.createResourceLoader();
      return resourceLoader.getResourceAsStream(filename);
   }

   /**
    * @return A list of supported file extensions
    */
   public String getFileExtensions()
   {
      return fileExtensions;
   }

   /**
    * Sets the file extensions this formatter should react to. All files not
    * having one of these extensions won't be processed.
    *
    * @param extensions A list of supported file extensions
    */
   public void setFileExtensions(String extensions)
   {
      this.fileExtensions = extensions;
   }

}

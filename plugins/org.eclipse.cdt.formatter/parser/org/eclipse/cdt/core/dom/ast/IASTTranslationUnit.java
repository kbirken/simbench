/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * The translation unit represents a compilable unit of source.
 * 
 * @author Doug Schaefer
 */
public interface IASTTranslationUnit extends IASTNode {

	/**
	 * <code>OWNED_DECLARATION</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTDeclaration</code>'s.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"IASTTranslationUnit.OWNED_DECLARATION - IASTDeclaration for IASTTranslationUnit"); //$NON-NLS-1$

	/**
	 * <code>SCANNER_PROBLEM</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTProblem</code>.
	 */
	public static final ASTNodeProperty SCANNER_PROBLEM = new ASTNodeProperty(
			"IASTTranslationUnit.SCANNER_PROBLEM - IASTProblem (scanner caused) for IASTTranslationUnit"); //$NON-NLS-1$

	/**
	 * <code>PREPROCESSOR_STATEMENT</code> represents the relationship between an <code>IASTTranslationUnit</code> and
	 * it's nested <code>IASTPreprocessorStatement</code>.
	 */
	public static final ASTNodeProperty PREPROCESSOR_STATEMENT = new ASTNodeProperty(
			"IASTTranslationUnit.PREPROCESSOR_STATEMENT - IASTPreprocessorStatement for IASTTranslationUnit"); //$NON-NLS-1$
    
	/**
	 * A translation unit contains an ordered sequence of declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Add declaration to translation unit. 
	 * 
	 * @param declaration <code>IASTDeclaration</code>
	 */
	public void addDeclaration(IASTDeclaration declaration);

	/**
	 * This returns the global scope for the translation unit.
	 * 
	 * @return the global scope
	 */
	public IScope getScope();

	/**
	 * Returns the list of declarations in this translation unit for the given
	 * binding. The list contains the IName nodes that declare the binding.
	 * These may be part of the AST or are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IName nodes for the binding's declaration
	 */
	public IName[] getDeclarations(IBinding binding);
    
	/**
	 * Returns the list of declarations in this translation unit for the given
	 * binding. The list contains the IASTName nodes that declare the binding.
	 * These are part of the AST no declarations are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IASTName nodes for the binding's declaration
	 */
	public IASTName[] getDeclarationsInAST(IBinding binding);

	/**
     * Returns the array of definitions in this translation unit for the given binding.
     * The array contains the IName nodes that define the binding.
	 * These may be part of the AST or are pulled in from the index.
     *  
     * @param binding
     * @return the definition of the IBinding
     */
    public IName[] getDefinitions(IBinding binding);

	/**
     * Returns the array of definitions in this translation unit for the given binding.
     * The array contains the IASTName nodes that define the binding.
	 * These are part of the AST no definitions are pulled in from the index.
	 * 
	 * @param binding
	 * @return Array of IASTName nodes for the binding's declaration
	 */
	public IASTName[] getDefinitionsInAST(IBinding binding);

	/**
	 * Returns the list of references in this translation unit to the given
	 * binding. This list contains the IName nodes that represent a use of
	 * the binding. They may be part of the AST or pulled in from the index.
	 * 
	 * @param binding
	 * @return List of IASTName nodes representing uses of the binding
	 */
	public IASTName[] getReferences(IBinding binding);

	/**
	 * @param offset
	 * @param length
	 * @return
	 */
	public IASTNodeLocation[] getLocationInfo(int offset, int length);

	/**
	 * Select the node in the treet that best fits the offset/length/file path. 
	 * 
	 * @param path - file name specified through path
	 * @param offset - location in the file as an offset
	 * @param length - length of selection
	 * @return <code>IASTNode</code> that best fits
	 */
	public IASTNode selectNodeForLocation(String path, int offset, int length);

	/**
	 * Get the macro definitions encountered in parsing this translation unit. 
	 * 
	 * @return <code>IASTPreprocessorMacroDefinition[]</code>
	 */
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions();

	/**
	 * Get builtin macro definitions used when parsing this translation unit.
	 * This includes macros obtained from the index. 
	 * 
	 * @return <code>IASTPreprocessorMacroDefinition[]</code>
	 */
	public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions();

	/**
	 * Get the #include directives encountered in parsing this translation unit.
	 * @return <code>IASTPreprocessorIncludeStatement[]</code>
	 */
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives();

	/**
	 * Get all preprocessor statements.
	 * 
	 * @return <code>IASTPreprocessorStatement[]</code>
	 */
	public IASTPreprocessorStatement[] getAllPreprocessorStatements();

	/**
	 * Get all preprocessor and scanner problems.
	 * @return <code>IASTProblem[]</code>
	 */
	public IASTProblem[] getPreprocessorProblems();

	/**
	 * For a given range of locations, return a String that represents what is there underneath the range.
	 * 
	 * @param locations A range of node locations
	 * @return A String signature.
	 */
	public String getUnpreprocessedSignature(IASTNodeLocation[] locations);

	/**
	 * Get the translation unit's full path.  
	 * @return String representation of path.
	 */
	public String getFilePath();
    
    /**
     * Flatten the node locations provided into a single file location.  
     * 
     * @param nodeLocations <code>IASTNodeLocation</code>s to flatten
     * @return null if not possible, otherwise, a file location representing where the macros are. 
     */
    public IASTFileLocation flattenLocationsToFile( IASTNodeLocation [] nodeLocations );
    
    public static final ASTNodeProperty EXPANSION_NAME = new ASTNodeProperty(
    "IASTTranslationUnit.EXPANSION_NAME - IASTName generated for macro expansions."); //$NON-NLS-1$
    
    
    public static interface IDependencyTree
    {
        public String getTranslationUnitPath();
        
        public static interface IASTInclusionNode
        {
            public IASTPreprocessorIncludeStatement getIncludeDirective();
            public IASTInclusionNode [] getNestedInclusions();
        }
        
        public IASTInclusionNode [] getInclusions();
    }
    
    /**
     * @return
     */
    public IDependencyTree getDependencyTree();

	/**
	 * @param offset
	 * @return
	 */
	public String getContainingFilename(int offset);
    
    
    /**
     * @return
     */
    public ParserLanguage getParserLanguage();
    
    /**
     * Return the Index associated with this translation unit.
     * 
     * @return the Index for this translation unit
     */
    public IIndex getIndex();
    
    /**
     * Set the Index to be used for this translation unit.
     * 
     * @param index
     */
    public void setIndex(IIndex index);
    
    /**
     * Set comments to translation unit.
     *   
     * @param comment
     */
    public void setComments(IASTComment[] comments);

	/**
	 * In case the ast was created in a way that supports comment parsing,
	 * all comments of the translation unit are returned. Otherwise an
	 * empty array will be supplied.
	 * 
	 * @return <code>IASTComment[]</code>
	 * @since 4.0
	 */
	public IASTComment[] getComments();
    
}

/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Anton Leherbauer (Wind River Systems)
 *    Emanuel Graf (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTPreprocessorSelectionResult;

/**
 * @author jcamelon
 */
public class LocationMap implements ILocationResolver, IScannerPreprocessorLog {

    
    public LocationMap() {
        tu = new _TranslationUnit();
        currentContext = tu;
    }
    
    public class MacroExpansionLocation implements IASTMacroExpansion {

        public MacroExpansionLocation(
                IASTPreprocessorMacroDefinition macroDefinition,
                IASTNodeLocation[] locations, int offset, int length) {
            this.definition = macroDefinition;
            this.locations = locations;
            this.offset = offset;
            this.length = length;
        }

        private final int length;

        private final int offset;

        private final IASTNodeLocation[] locations;

        private final IASTPreprocessorMacroDefinition definition;

        public IASTPreprocessorMacroDefinition getMacroDefinition() {
            return definition;
        }

        public IASTNodeLocation[] getExpansionLocations() {
            return locations;
        }

        public int getNodeOffset() {
            return offset;
        }

        public int getNodeLength() {
            return length;
        }

        public IASTFileLocation asFileLocation() {
            return rootNode.flattenLocationsToFile(locations);
        }
        
        public String toString() {
            
            return "Macro Expansion " + definition.getName().toString() + " flattened to " + asFileLocation().toString(); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }
    
    public class FunctionMacroExpansionLocation extends MacroExpansionLocation{
    	
    	private char[][] actParams;

		/**
		 * @param macroDefinition
		 * @param locations
		 * @param offset
		 * @param length
		 */
		public FunctionMacroExpansionLocation(IASTPreprocessorMacroDefinition macroDefinition, IASTNodeLocation[] locations, int offset, int length, char[][] actParameters) {
			super(macroDefinition, locations, offset, length);
			this.actParams = actParameters;
		}
		
		public char[][] getActualParameters() {
			return actParams;
		}
    	
    }

    private static final String NOT_VALID_MACRO = "Not a valid macro selection"; //$NON-NLS-1$

    private static final String TU_INCLUDE_NOT_FOUND = "File searching does not match TU or #includes."; //$NON-NLS-1$

    /**
     * @author jcamelon
     */
    public static class ASTEndif extends ASTNode implements
            IASTPreprocessorEndifStatement {

    }

    /**
     * @author jcamelon
     */
    public static class ASTElif extends ASTNode implements
            IASTPreprocessorElifStatement {

        private final boolean taken;
        private char[] condition;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement#taken()
         */
        public boolean taken() {
            return taken;
        }

        /**
         * @param taken
         */
        public ASTElif(boolean taken, char[] condition) {
            this.taken = taken;
            this.condition = condition;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement#getCondition()
		 */
		public char[] getCondition() {
			return condition;
		}

    }

    /**
     * @author jcamelon
     */
    public static class ASTElse extends ASTNode implements
            IASTPreprocessorElseStatement {
        private final boolean taken;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement#taken()
         */
        public boolean taken() {
            return taken;
        }

        /**
         * @param taken
         */
        public ASTElse(boolean taken) {
            this.taken = taken;
        }

    }

    /**
     * @author jcamelon
     */
    public static class ASTIfndef extends ASTNode implements
            IASTPreprocessorIfndefStatement {

        private final boolean taken;
        private char[] condition;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement#taken()
         */
        public boolean taken() {
            return taken;
        }

        /**
         * @param taken
         */
        public ASTIfndef(boolean taken, char[] condition) {
            this.taken = taken;
            this.condition = condition;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement#getCondition()
		 */
		public char[] getCondition() {
			return condition;
		}

    }

    /**
     * @author jcamelon
     */
    public static class ASTIfdef extends ASTNode implements
            IASTPreprocessorIfdefStatement {

        private final boolean taken;
        
        private char[] condition;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement#taken()
         */
        public boolean taken() {
            return taken;
        }

        /**
         * @param taken
         */
        public ASTIfdef(boolean taken, char[] condition) {
            this.taken = taken;
            this.condition = condition;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement#getCondition()
		 */
		public char[] getCondition() {
			return condition;
		}

    }

    /**
     * @author jcamelon
     */
    public static class ASTIf extends ASTNode implements
            IASTPreprocessorIfStatement {

        private final boolean taken;
		private char[] condition;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement#taken()
         */
        public boolean taken() {
            return taken;
        }

        /**
         * @param taken
         */
        public ASTIf(boolean taken, char[] condition) {
            this.taken = taken;
            this.condition = condition;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement#getCondition()
		 */
		public char[] getCondition() {
			return condition;
		}

    }

    /**
     * @author jcamelon
     */
    public static class ASTError extends ASTNode implements
            IASTPreprocessorErrorStatement {
    	
    	private char[] msg;
    	
    	
    	
    	public ASTError(char[] msg) {
			super();
			this.msg = msg;
		}

		public char[] getMessage() {
    		return msg;
    	}

    }

    public static class ASTWarning extends ASTNode implements
    IASTPreprocessorErrorStatement {

    	private char[] msg;

    	public ASTWarning(char[] msg) {
    		super();
    		this.msg = msg;
    	}

    	public char[] getMessage() {
    		return msg;
    	}
    }

    /**
     * @author jcamelon
     */
    public static class ASTPragma extends ASTNode implements
            IASTPreprocessorPragmaStatement {
    	
    	private char[] msg;
    	
    	

		public ASTPragma(char[] msg) {
			super();
			this.msg = msg;
		}



		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement#getMessage()
		 */
		public char[] getMessage() {
			return msg;
		}

    }

    /**
     * @author jcamelon
     */
    public static class ASTUndef extends ASTNode implements
            IASTPreprocessorUndefStatement {

        public ASTUndef( IASTName n )
        {
            this.n = n;
            n.setPropertyInParent( IASTPreprocessorUndefStatement.MACRO_NAME );
            n.setParent(this);
        }
        
        private final IASTName n;

        public IASTName getMacroName() {
            return n;
        }

    }

    /**
     * @author jcamelon
     */
    protected static class _Endif extends _Context implements
            _IPreprocessorDirective {

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Endif(_CompositeContext parent, int startOffset, int endOffset) {
            super(parent, startOffset, endOffset);
            // TODO Auto-generated constructor stub
        }

    }

    /**
     * @author jcamelon
     */
    protected static class _Elif extends _Context implements
            _IPreprocessorDirective {

        public final boolean taken;
        public char[] condition;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Elif(_CompositeContext parent, int startOffset, int endOffset,
                boolean taken, char[] condition) {
            super(parent, startOffset, endOffset);
            this.taken = taken;
            this.condition = condition;
        }

    }

    /**
     * @author jcamelon
     */
    protected static class _Ifdef extends _Context implements
            _IPreprocessorDirective {

        public final boolean taken;
		public char[] condition;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Ifdef(_CompositeContext parent, int startOffset, int endOffset,
                boolean taken, char[] condition) {
            super(parent, startOffset, endOffset);
            this.taken = taken;
            this.condition = condition;
        }

    }

    /**
     * @author jcamelon
     */
    protected static class _Ifndef extends _Context implements
            _IPreprocessorDirective {

        public final boolean taken;
		public char[] condition;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Ifndef(_CompositeContext parent, int startOffset,
                int endOffset, boolean taken, char[] condition) {
            super(parent, startOffset, endOffset);
            this.taken = taken;
            this.condition = condition;
        }

    }

    /**
     * @author jcamelon
     */
    protected static class _Error extends _Context implements
            _IPreprocessorDirective {
    	
    	char[] msg;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         * @param msg 
         */
        public _Error(_CompositeContext parent, int startOffset, int endOffset, char[] msg) {
            super(parent, startOffset, endOffset);
            this.msg = msg;
        }

    }

    protected static class _Warning extends _Context implements _IPreprocessorDirective {
    	char[] msg;
     	public _Warning(_CompositeContext parent, int startOffset, int endOffset, char[] msg) {
    		super(parent, startOffset, endOffset);
    		this.msg = msg;
    	}
    }
    
    /**
     * @author jcamelon
     */
    protected static class _Pragma extends _Context implements
            _IPreprocessorDirective {
    	
    	char[] msg;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Pragma(_CompositeContext parent, int startOffset, int endOffset, char[] msg) {
            super(parent, startOffset, endOffset);
            this.msg = msg;
        }
        

    }

    /**
     * @author jcamelon
     */
    protected class _If extends _Context implements _IPreprocessorDirective {

        public final boolean taken;
		public char[] condition;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _If(_CompositeContext parent, int startOffset, int endOffset,
                boolean taken, char[] condition) {
            super(parent, startOffset, endOffset);
            this.taken = taken;
            this.condition = condition;
        }

    }

    /**
     * @author jcamelon
     */
    protected static class _Else extends _Context implements
            _IPreprocessorDirective {

        public final boolean taken;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Else(_CompositeContext parent, int startOffset, int endOffset,
                boolean taken) {
            super(parent, startOffset, endOffset);
            this.taken = taken;
        }

    }

    /**
     * Represents an include directive without sub-contexts.
     * This allows to model inactive includes and unresolved includes.
     */
    protected static class _Include extends _Context implements
    		_IPreprocessorIncludeDirective {

        public final int fNameOffset;
        public final int fNameEndOffset;
		public final char[] fName;
		public final char[] fPath;
		public final boolean fSystemInclude;
    	public final boolean fIsActive;
    	public final boolean fIsResolved;

		public _Include(_CompositeContext parent, int startOffset, int endOffset, int nameOffset, int nameEndoffset,
				char[] name, char[] path, boolean systemInclude, boolean active, boolean resolved) {
            super(parent, startOffset, endOffset);
            fNameOffset= nameOffset;
            fNameEndOffset= nameEndoffset;
            fName= name;
            fPath= path == null ? name : path;
            fSystemInclude= systemInclude;
            fIsActive= active;
            fIsResolved= resolved;
        }

    }

    /**
     * @author jcamelon
     */
    public static class ASTInclusionStatement extends ASTNode implements
            IASTPreprocessorIncludeStatement {

        private final char[] path;
		private IASTName fName;

        public int startOffset;
        public int endOffset;
		private boolean fSystemInclude;
		private boolean fIsActive= true;
		private boolean fIsResolved= true;

        /**
         * @param cs
         */
        public ASTInclusionStatement(char[] cs) {
            this.path = cs;
        }

        public String getPath() {
            return new String(path);
        }
        
        public String toString() {
            return getPath();
        }

		public IASTName getName() {
			return fName;
		}
		
		public void setName(IASTName name) {
			fName= name;
		}
		
		public boolean isSystemInclude() {
			return fSystemInclude;
		}
		
		public void setSystemInclude(boolean val) {
			fSystemInclude= val;
		}

		public boolean isActive() {
			return fIsActive;
		}
		
		public void setActive(boolean val) {
			fIsActive= val;
		}

		public boolean isResolved() {
			return fIsResolved;
		}
		
		public void setResolved(boolean val) {
			fIsResolved= val;
		}
    }

    /**
     * @author jcamelon
     */
    public static class ASTFunctionMacro extends ASTNode implements
            IASTPreprocessorFunctionStyleMacroDefinition {

        private IASTName name;
        private String expansion;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroDefinition#getParameters()
         */
        public IASTFunctionStyleMacroParameter[] getParameters() {
            if (parameters == null)
                return IASTFunctionStyleMacroParameter.EMPTY_PARAMETER_ARRAY;
            removeNullParameters();
            return parameters;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroDefinition#addParameter(org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter)
         */
        public void addParameter(IASTFunctionStyleMacroParameter parm) {
            if (parameters == null) {
                parameters = new IASTFunctionStyleMacroParameter[DEFAULT_PARMS_LIST_SIZE];
                currentIndex = 0;
            }
            if (parameters.length == currentIndex) {
                IASTFunctionStyleMacroParameter[] old = parameters;
                parameters = new IASTFunctionStyleMacroParameter[old.length * 2];
                for (int i = 0; i < old.length; ++i)
                    parameters[i] = old[i];
            }
            parameters[currentIndex++] = parm;
        }

        private void removeNullParameters() {
            int nullCount = 0;
            for (int i = 0; i < parameters.length; ++i)
                if (parameters[i] == null)
                    ++nullCount;
            if (nullCount == 0)
                return;
            IASTFunctionStyleMacroParameter[] old = parameters;
            int newSize = old.length - nullCount;
            parameters = new IASTFunctionStyleMacroParameter[newSize];
            for (int i = 0; i < newSize; ++i)
                parameters[i] = old[i];
            currentIndex = newSize;
        }

        private int currentIndex = 0;

        private IASTFunctionStyleMacroParameter[] parameters = null;

        private static final int DEFAULT_PARMS_LIST_SIZE = 2;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getName()
         */
        public IASTName getName() {
            return name;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setName(org.eclipse.cdt.core.dom.ast.IASTName)
         */
        public void setName(IASTName name) {
            this.name = name;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getExpansion()
         */
        public String getExpansion() {
            return expansion;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setExpansion(java.lang.String)
         */
        public void setExpansion(String exp) {
            this.expansion = exp;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
         */
        public int getRoleForName(IASTName n) {
            if (name == n)
                return r_definition;
            return r_unclear;
        }

    }

    public static interface _IPreprocessorDirective {
    }
    public static interface _IPreprocessorIncludeDirective extends _IPreprocessorDirective {
    }

    protected class _Undef extends _Context implements
            _IPreprocessorDirective {
        public final char[] name;
        public final int nameOffset;
        private IASTName expansionName;
        public final IMacroDefinition macroDefn;
        
        public IASTName getName()
        {
            if( expansionName == null )
            {
                expansionName = new ASTMacroName( name );
                expansionName.setParent( rootNode );
                expansionName.setPropertyInParent( IASTPreprocessorUndefStatement.MACRO_NAME );
                ((ASTNode)expansionName).setOffsetAndLength( context_directive_start, name.length );
            }
            return expansionName;
        }
        
        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Undef(_CompositeContext parent, int startOffset, int endOffset,
                char[] name, int nameOffset, IMacroDefinition macro ) {
            super(parent, startOffset, endOffset);
            this.name = name;
            this.nameOffset = nameOffset;
            this.macroDefn = macro;
        }

    }

    /**
     * @author jcamelon
     */
    public class ASTMacroName extends ASTNode implements IASTName {
    	private final char[] name;
    	private final IASTFileLocation fileLocation;
    	private IMacroBinding binding = null;
    	
        public ASTMacroName(char[] n) {
        	this(n, null);
        }

        public ASTMacroName(char [] n, IASTFileLocation fileLocation) {
        	this.name = n;
        	this.fileLocation = fileLocation;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
         */
        public IBinding resolveBinding() {
            if( binding == null )
            {
                binding = resolveBindingForMacro( name, getOffset() );
            }
            return binding; 
        }


    	public IASTCompletionContext getCompletionContext() {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTName#toCharArray()
         */
        public char[] toCharArray() {
            return name;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return new String(name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTName#isDeclaration()
         */
        public boolean isDeclaration() {
            if (getParent() instanceof IASTPreprocessorMacroDefinition
                    && getPropertyInParent() == IASTPreprocessorMacroDefinition.MACRO_NAME)
                return true;
            return false;
        }
        
        public boolean isDefinition() {
            return isDeclaration();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTName#isReference()
         */
        public boolean isReference() {
            return !isDeclaration();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTName#getBinding()
         */
        public IBinding getBinding() {
            return binding;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTName#setBinding(org.eclipse.cdt.core.dom.ast.IBinding)
         */
        public void setBinding(IBinding binding) {
            //do nothing
        }
        
        public IASTFileLocation getFileLocation() {
        	return fileLocation != null ? fileLocation : super.getFileLocation();
        }
        
    	public ILinkage getLinkage() {
    		return Linkage.NO_LINKAGE;
    	}
    }

    public class ASTIncludeName extends ASTNode implements IASTName {
        private final char[] name;
       
        public ASTIncludeName(char[] n) {
            this.name = n;
        }

        public IBinding resolveBinding() {
            return null; 
        }
    	public IASTCompletionContext getCompletionContext() {
    		return null;
    	}

        public char[] toCharArray() {
            return name;
        }

        public String toString() {
            return new String(name);
        }

        public boolean isDeclaration() {
            return false;
        }
        
        public boolean isDefinition() {
        	return false;
        }

        public boolean isReference() {
            return false;
        }

        public IBinding getBinding() {
            return null;
        }

        public void setBinding(IBinding binding) {
        }
        
    	public ILinkage getLinkage() {
    		return Linkage.NO_LINKAGE;
    	}
    }

    /**
     * @author jcamelon
     */
    public static class ASTObjectMacro extends ASTNode implements
            IASTPreprocessorObjectStyleMacroDefinition {

        private IASTName name;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
         */
        public int getRoleForName(IASTName n) {
            if (name == n)
                return r_definition;
            return r_unclear;
        }

        private String expansion;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getName()
         */
        public IASTName getName() {
            return name;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setName(org.eclipse.cdt.core.dom.ast.IASTName)
         */
        public void setName(IASTName name) {
            this.name = name;

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#getExpansion()
         */
        public String getExpansion() {
            return expansion;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTMacroDefinition#setExpansion(java.lang.String)
         */
        public void setExpansion(String exp) {
            this.expansion = exp;
        }

    }

    public abstract static class Location implements IASTNodeLocation {
        private final int nodeOffset;

        private final int nodeLength;

        /**
         * @param offset
         * @param length
         */
        public Location(int offset, int length) {
            nodeOffset = offset;
            nodeLength = length;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeOffset()
         */
        public int getNodeOffset() {
            return nodeOffset;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeLength()
         */
        public int getNodeLength() {
            return nodeLength;
        }

    }

    public static class ASTFunctionMacroParameter extends ASTNode
            implements IASTFunctionStyleMacroParameter {
        private String value;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter#getParameter()
         */
        public String getParameter() {
            return value;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter#setParameter(java.lang.String)
         */
        public void setParameter(String value) {
            this.value = value;
        }

    }

    public class FileLocation extends Location implements
            IASTFileLocation {

        private String fileName;
        private _CompositeFileContext _fileContext;

        /**
         * @param length
         * @param offset
         * @param tu_filename
         * 
         */
        public FileLocation(char[] tu_filename, int offset, int length) {
            super(offset, length);
            fileName = new String(tu_filename);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getFileName()
         */
        public String getFileName() {
            return fileName;
        }

        public IASTFileLocation asFileLocation() {
            return this;
        }

        public int getStartingLineNumber() {
            _CompositeFileContext i = getFileContext();
            if( i != null )
                return i.getLineNumber( getNodeOffset() );
            return 0;
        }

        private _CompositeFileContext getFileContext() {
            if( _fileContext == null )
            {
                if( fileName.equals( getTranslationUnitPath() ))
                    _fileContext = tu;
                else
                    _fileContext = findInclusion( tu, fileName );
            }
            return _fileContext;
        }

        public int getEndingLineNumber() {
            _CompositeFileContext i = getFileContext();
            if( i != null )
                return i.getLineNumber( getNodeOffset() + getNodeLength());
            return 0;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer( fileName );
            buffer.append(" line " ); //$NON-NLS-1$
            buffer.append( getStartingLineNumber() );
            buffer.append( " to " ); //$NON-NLS-1$
            buffer.append( getEndingLineNumber() );
            return buffer.toString();
        }
        
    }

    protected static class _Context {
        /**
         * @param startOffset
         * @param endOffset
         */
        public _Context(_CompositeContext parent, int startOffset, int endOffset) {
            this.context_directive_start = startOffset;
            this.context_directive_end = endOffset;
            this.context_ends = endOffset;
            this.parent = parent;
        }

        /**
         * Global offset of start of directive (inclusive).
         */
        public int context_directive_start;
        /**
         * Global offset of end of directive (exclusive).
         */
        public int context_directive_end;	
        /**
         * Global offset of end of context introduced by directive (exclusive).
         */
        public int context_ends;			
        public _CompositeContext parent;

        public _CompositeContext getParent() {
            return parent;
        }

        public int getDirectiveStart() {
        	return context_directive_start;
        }
        
        public int getDirectiveLength() {
        	return context_directive_end - context_directive_start;
        }
        
        public int getContextStart() {
        	return context_directive_end;
        }
        
        public int getContextLength() {
        	return context_ends- context_directive_end;
        }
            	
        public final boolean contains(int offset) {
            return (offset >= context_directive_start && offset < context_ends);
        }

        public final boolean containsInContext(int offset) {
            return (offset >= context_directive_end && offset < context_ends);
        }

        public boolean containsInDirective(int offset, int length) {
        	return context_directive_start <= offset && offset + length <= context_directive_end;
        }

        public boolean hasAncestor(_Context cc) {
            _Context p = parent;
            while (p != null) {
                if (p == cc)
                    return true;
                p = p.parent;
            }
            return false;
        }

		public _CompositeFileContext getContainingFileContext() {
			if( this instanceof _CompositeFileContext )
				return (_CompositeFileContext) this;
			_CompositeContext result = getParent();
			while( !( result instanceof _CompositeFileContext ) )
				result = result.getParent();
			return (_CompositeFileContext) result;
		}
    }

    protected static class _CompositeContext extends _Context {

        public _CompositeContext(_CompositeContext parent, int startOffset,
                int endOffset) {
            super(parent, startOffset, endOffset);
        }

        private static final int DEFAULT_SUBCONTEXT_ARRAY_SIZE = 8;

        private _Context[] subContexts = null;

        private static final _Context[] EMPTY_CONTEXT_ARRAY = new _Context[0];

        public _Context[] getSubContexts() {
            if (subContexts == null)
                return EMPTY_CONTEXT_ARRAY;
            final int length= subContexts.length;
            if (length > 0 && subContexts[length-1] == null) {
            	subContexts = (_Context[]) ArrayUtil.trim(_Context.class, subContexts);
            }
            return subContexts;
        }

        public void addSubContext(_Context c) {
            if (subContexts == null)
                subContexts = new _Context[DEFAULT_SUBCONTEXT_ARRAY_SIZE];
            subContexts = (_Context[]) ArrayUtil.append(_Context.class, subContexts, c);
        }

        public boolean hasSubContexts() {
            if (subContexts == null)
                return false;
            for (int i = 0; i < subContexts.length; ++i)
                if (subContexts[i] != null)
                    return true;
            return false;
        }

        public _Context findContextContainingOffset(int offset) {
            if (subContexts != null)
            {
	            int left = 0;
	            int right = subContexts.length - 1;
	            int middle = 0;
	            while (left <= right) {
	            	middle = (left+right) / 2;
	            	_Context c = subContexts[middle];
	            	if (c== null || offset < c.context_directive_start) {
	            		right= middle-1;
	            	}
	            	else if (offset >= c.context_ends) {
	            		left= middle+1;
	            	}
	            	else {
	            		if (c instanceof _CompositeContext) {
	                        final _CompositeContext compositeContext = (_CompositeContext) c;
							_Context nested= compositeContext.findContextContainingOffset(offset);
	                        if (nested != null) {
	                        	return nested;
	                        }
	                    }
	                    return c;
	            	}
	            }
            }
            // if no sub context contains this, do it this way
            if ((offset >= context_directive_start && offset < context_ends))
                return this;
            return null;
        }
        

        public int getNumberOfContexts() {
            final _Context[] contextz = getSubContexts();
            int result = contextz.length;
            for (int i = 0; i < contextz.length; ++i)
                if (contextz[i] instanceof _CompositeContext)
                    result += ((_CompositeContext) contextz[i])
                            .getNumberOfContexts();
            return result;
        }

        public int getOffsetInContext(int globalOffset) {
            int externalCount= 0;
            if (subContexts != null) {
            	for (int i = 0; i < subContexts.length; ++i) {
            		_Context subC = subContexts[i];
            		if (subC == null || globalOffset < subC.getContextStart()) {
            			break;
            		}
            		if (subC instanceof _CompositeContext) {
            			externalCount+= subC.getContextLength();
            		}
            	}
            }
            final int result = globalOffset - externalCount - getContextStart();
            return ((result < 0) ? 0 : result);
        }
    }    

    protected static class _CompositeFileContext extends _CompositeContext {
        public CodeReader reader;
        private int[] lineOffsetCache;
        private int numberOfLines;
        
        public _CompositeFileContext(_CompositeContext parent, int startOffset,
                int endOffset, CodeReader reader) {
            super(parent, startOffset, endOffset);
            this.reader = reader;
        }
        
        public _CompositeFileContext(_CompositeContext parent, int startOffset,
               int endOffset ) {
            super(parent, startOffset, endOffset);
        }
        
        public int getLineNumber(int nodeOffset) {
        	if( nodeOffset >= reader.buffer.length )
        		return 1;
        	if (lineOffsetCache == null) {
        		createLineOffsetCache();
        	}
    		int left= 0;
    		int right= numberOfLines - 1;
    		int mid= 0;

    		while (left < right) {
    			mid= (left + right) / 2;
    			int lineOffset= lineOffsetCache[mid];
    			if (nodeOffset < lineOffset) {
    				if (left == mid)
    					right= mid;
    				else
    					right= mid - 1;
    			} else if (nodeOffset > lineOffset) {
    				if (right == mid)
    					left= mid;
    				else
    					left= mid + 1;
    			} else if (nodeOffset == lineOffset) {
    				left= right= mid;
    			}
    		}
    		if (nodeOffset < lineOffsetCache[left])
    			--left;
    		return left + 1;
        }

		private void createLineOffsetCache() {
			final char[] buffer= reader.buffer;
			int[] lineOffsets= new int[buffer.length / 30 + 1];
			int lines= 1;
			lineOffsets[0]= 0;
			for (int i = 0; i < buffer.length; ++i) {
				// TODO consider '\r' line endings?
				if (buffer[i] == '\n') {
					if (lineOffsets.length == lines) {
						final int ratio= Math.min(Math.max(i / lines, 1), 20);
						int[] tmp= new int[lines + (buffer.length - i) / ratio + 1];
						System.arraycopy(lineOffsets, 0, tmp, 0, lines);
						lineOffsets= tmp;
					}
					lineOffsets[lines++]= i + 1;
				}
			}
			lineOffsetCache= lineOffsets;
			numberOfLines= lines;
		}
    }

    protected static class _Inclusion extends _CompositeFileContext implements
            _IPreprocessorIncludeDirective {

        public final int fNameOffset;
        public final int fNameEndOffset;
		public final char[] fName;
		public boolean fSystemInclude;

		public _Inclusion(_CompositeContext parent, CodeReader reader,
                int startOffset, int endOffset, int nameOffset, int nameEndoffset, char[] name, boolean systemInclude) {
            super(parent, startOffset, endOffset, reader);
            fNameOffset= nameOffset;
            fNameEndOffset= nameEndoffset;
            fName= name;
            fSystemInclude= systemInclude;
        }
    }

    protected static class _TranslationUnit extends _CompositeFileContext {

        public _TranslationUnit() {
            super(null, 0, 0 );
        }

        IMacroDefinition [] builtins = new IMacroDefinition[2];
        private int builtinsPos=-1;
        
        public void addBuiltinMacro(IMacroDefinition def) {
        	if (def != null) {
        		builtins = (IMacroDefinition[]) ArrayUtil.append( IMacroDefinition.class, builtins, ++builtinsPos, def );	
        	}
        }
        
        public IMacroDefinition [] getBuiltinMacroDefinitions()
        {
            builtins = (IMacroDefinition[]) ArrayUtil.removeNullsAfter( IMacroDefinition.class, builtins, builtinsPos );
            return builtins;
        }

    }

    protected static class _MacroDefinition extends _Context implements
            IMacroDefinition {

        public _MacroDefinition(_CompositeContext parent, int startOffset,
                int endOffset, char[] name, int nameOffset, char[] expansion) {
            super(parent, startOffset, endOffset);
            this.name = name;
            this.expansion = expansion;
            this.nameOffset = nameOffset;
        }

        public _MacroDefinition(_CompositeContext parent, int startOffset,
                int endOffset, char[] name, int nameOffset, IMacro macro) {
            super(parent, startOffset, endOffset);
            this.name = name;
            this.macro = macro;
            this.nameOffset = nameOffset;
        }

        public final char[] name;
        public final int nameOffset;
        public IASTPreprocessorMacroDefinition astNode;
        
        // expansions can be in the IMacro if there is one
        private char[] expansion;
        private IMacro macro;

        private IMacroBinding bind;
        
        public char[] getName() {
            return name;
        }

        public char[] getExpansion() {
        	if (expansion == null && macro != null)
        		expansion = macro.getExpansion();
            return expansion;
        }

        public IASTFileLocation getFileLocation() {
        	return macro != null && macro instanceof IIndexMacro
        		? ((IIndexMacro)macro).getFileLocation()
        		: null;
        }
        
        public IMacroBinding getBinding() {
            return bind;
        }

        public void setBinding(IMacroBinding b) {
            this.bind = b;
        }

    }

    protected static class _ObjectMacroDefinition extends _MacroDefinition
            implements _IPreprocessorDirective {
        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         * @param name
         *            TODO
         * @param expansion
         *            TODO
         */
        public _ObjectMacroDefinition(_CompositeContext parent,
                int startOffset, int endOffset, char[] name, int nameOffset,
                char[] expansion) {
            super(parent, startOffset, endOffset, name, nameOffset, expansion);
        }
        
        public _ObjectMacroDefinition(_CompositeContext parent,
                int startOffset, int endOffset, char[] name, int nameOffset,
                IMacro macro) {
            super(parent, startOffset, endOffset, name, nameOffset, macro);
        }

    }

    protected static class _FunctionMacroDefinition extends _MacroDefinition
            implements _IPreprocessorDirective {

        private final char[][] parms;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         * @param parameters
         */
        public _FunctionMacroDefinition(_CompositeContext parent,
                int startOffset, int endOffset, char[] name, int nameOffset,
                char[] expansion, char[][] parameters) {
            super(parent, startOffset, endOffset, name, nameOffset, expansion);
            this.parms = parameters;
        }

        public _FunctionMacroDefinition(_CompositeContext parent,
                int startOffset, int endOffset, char[] name, int nameOffset,
                IMacro macro, char[][] parameters) {
            super(parent, startOffset, endOffset, name, nameOffset, macro);
            this.parms = parameters;
        }

        public char[][] getParms() {
            return parms;
        }
    }

    protected static class _Problem extends _Context {
        public final IASTProblem problem;

        /**
         * @param parent
         * @param startOffset
         * @param endOffset
         */
        public _Problem(_CompositeContext parent, int startOffset,
                int endOffset, IASTProblem problem) {
            super(parent, startOffset, endOffset);
            this.problem = problem;
        }

    }

    protected class _MacroExpansion extends _CompositeContext {
        public final IMacroDefinition definition;
        private IASTName expansionName;

        public _MacroExpansion(_CompositeContext parent, int startOffset,
                int endOffset, IMacroDefinition definition) {
            super(parent, startOffset, endOffset);
            this.definition = definition;
        }
        
        public IASTName getName()
        {
            if( expansionName == null )
            {
                expansionName = new ASTMacroName( definition.getName() );
                expansionName.setParent( rootNode );
                expansionName.setPropertyInParent( IASTTranslationUnit.EXPANSION_NAME );
                ((ASTNode)expansionName).setOffsetAndLength(context_directive_start, getDirectiveLength());
            }
            return expansionName;
        }
    }

    protected class _ObjectMacroExpansion extends _MacroExpansion {

        public _ObjectMacroExpansion(_CompositeContext parent, int startOffset,
                int endOffset, IMacroDefinition definition) {
            super(parent, startOffset, endOffset, definition);
        }

    }

    protected class _FunctionMacroExpansion extends _MacroExpansion {
        public final char[][] args;
        public final char[][] actArgs;


        public _FunctionMacroExpansion(_CompositeContext parent,
                int startOffset, int endOffset, IMacroDefinition definition,
                char[][] args, char[][] actParameters) {
            super(parent, startOffset, endOffset, definition);
            this.args = args;
            this.actArgs = actParameters;
        }

    }

    protected _TranslationUnit tu;

    protected _CompositeContext currentContext;

    private static final IASTProblem[] EMPTY_PROBLEMS_ARRAY = new IASTProblem[0];

    private static final IASTNodeLocation[] EMPTY_LOCATION_ARRAY = new IASTNodeLocation[0];

    private static final IASTPreprocessorMacroDefinition[] EMPTY_MACRO_DEFINITIONS_ARRAY = new IASTPreprocessorMacroDefinition[0];

    private static final IASTPreprocessorIncludeStatement[] EMPTY_INCLUDES_ARRAY = new IASTPreprocessorIncludeStatement[0];

    private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_ARRAY = new IASTPreprocessorStatement[0];

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getMacroDefinitions()
     */
    public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
        int size = collectContexts(V_MACRODEFS, tu, null, 0);
        if (size == 0)
            return EMPTY_MACRO_DEFINITIONS_ARRAY;
        _Context[] contexts = new _Context[size];
        collectContexts(V_MACRODEFS, tu, contexts, 0);

        IASTPreprocessorMacroDefinition[] result = new IASTPreprocessorMacroDefinition[contexts.length];
        for (int i = 0; i < contexts.length; ++i) 
            result[i] = createASTMacroDefinition((_MacroDefinition) contexts[i]);
        

        return result;
    }

    public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
    	IMacroDefinition[] mdefs= tu.getBuiltinMacroDefinitions();
        if (mdefs.length == 0)
            return EMPTY_MACRO_DEFINITIONS_ARRAY;
        ArrayList result= new ArrayList(mdefs.length);
        for (int i = 0; i < mdefs.length; i++) {
			IMacroDefinition mdef = mdefs[i];
			if (mdef instanceof _MacroDefinition) {
				result.add(createASTMacroDefinition((_MacroDefinition) mdef));
			}
        }
        return (IASTPreprocessorMacroDefinition[]) result.toArray(new IASTPreprocessorMacroDefinition[result.size()]);
    }

    public IMacroBinding resolveBindingForMacro(char[] name, int offset ) {
        _Context search = findContextForOffset( offset );
        IMacroDefinition macroDefinition = null;
        if (search instanceof _MacroDefinition) {
            _MacroDefinition macroDef = (_MacroDefinition) search;
            if( CharArrayUtils.equals( name, macroDef.name) && offset == macroDef.nameOffset )
                macroDefinition = macroDef;
        }
        else if (search instanceof _MacroExpansion ) {
            _MacroExpansion expansion = (_MacroExpansion) search;
            macroDefinition = expansion.definition;
        }
        if( macroDefinition == null )
            return null;
        if( macroDefinition.getBinding() == null )
            macroDefinition.setBinding( new MacroBinding( name, rootNode.getScope(), macroDefinition ) );
        return macroDefinition.getBinding();
    }

    /**
     * @param d
     * @return
     */
    private IASTPreprocessorMacroDefinition createASTMacroDefinition(
            _MacroDefinition d) {
        IASTPreprocessorMacroDefinition r = null;
        if (d instanceof _FunctionMacroDefinition) {
            IASTPreprocessorFunctionStyleMacroDefinition f = new ASTFunctionMacro();
            char[][] parms = ((_FunctionMacroDefinition) d).getParms();
            for (int j = 0; j < parms.length; ++j) {
                IASTFunctionStyleMacroParameter parm = new ASTFunctionMacroParameter();
                parm.setParameter(new String(parms[j]));
                f.addParameter(parm);
                parm.setParent(f);
                parm
                        .setPropertyInParent(IASTPreprocessorFunctionStyleMacroDefinition.PARAMETER);
            }
            r = f;
        }
        else {
        	r = new ASTObjectMacro();
        }
        
        IASTName name = new ASTMacroName(d.name, d.getFileLocation());
        name.setPropertyInParent(IASTPreprocessorMacroDefinition.MACRO_NAME);
        name.setParent(r);
        ((ASTNode) name).setOffsetAndLength(d.nameOffset, d.name.length);
        r.setName(name);
        r.setExpansion(new String(d.getExpansion()));
        ((ASTNode) r).setOffsetAndLength(d.context_directive_start, d.getDirectiveLength());
        d.astNode = r;
		r.setParent(rootNode);
		r.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);

        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getIncludeDirectives()
     */
    public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
        int size = collectContexts(V_INCLUDE_DIRECTIVES, tu, null, 0);
        if (size == 0)
            return EMPTY_INCLUDES_ARRAY;
        _Context[] contexts = new _Context[size];
        collectContexts(V_INCLUDE_DIRECTIVES, tu, contexts, 0);
        IASTPreprocessorIncludeStatement[] result = new IASTPreprocessorIncludeStatement[size];
        for (int i = 0; i < size; ++i) 
            result[i] = (IASTPreprocessorIncludeStatement)createPreprocessorStatement(contexts[i]);

        return result;
    }

    /**
     * @param inc
     * @return
     */
    private IASTPreprocessorIncludeStatement createASTInclusion(_Inclusion inc) {
    	ASTInclusionStatement result = new ASTInclusionStatement(inc.reader.filename);
        result.setOffsetAndLength(inc.context_directive_start, inc.getDirectiveLength());
        result.startOffset = inc.getContextStart();
        result.endOffset = inc.context_directive_end;
        ASTIncludeName name= new ASTIncludeName(inc.fName);
        name.setPropertyInParent(IASTPreprocessorIncludeStatement.INCLUDE_NAME);
        name.setParent(result);
        name.setOffsetAndLength(inc.fNameOffset, inc.fNameEndOffset-inc.fNameOffset);
        result.setName(name);
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
		result.setSystemInclude(inc.fSystemInclude);
		result.setActive(true);
		result.setResolved(true);
        return result;
    }

    private IASTPreprocessorIncludeStatement createASTInclusion(_Include inc) {
    	ASTInclusionStatement result = new ASTInclusionStatement(inc.fPath);
        result.setOffsetAndLength(inc.context_directive_start, inc.getDirectiveLength());
        result.startOffset = inc.getContextStart();
        result.endOffset = inc.context_directive_end;
        ASTIncludeName name= new ASTIncludeName(inc.fName);
        name.setPropertyInParent(IASTPreprocessorIncludeStatement.INCLUDE_NAME);
        name.setParent(result);
        name.setOffsetAndLength(inc.fNameOffset, inc.fNameEndOffset-inc.fNameOffset);
        result.setName(name);
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
		result.setSystemInclude(inc.fSystemInclude);
		result.setActive(inc.fIsActive);
		result.setResolved(inc.fIsResolved);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getAllPreprocessorStatements()
     */
    public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
        int size = collectContexts(V_PREPROCESSOR, tu, null, 0);
        if (size == 0)
            return EMPTY_PREPROCESSOR_ARRAY;
        _Context[] contexts = new _Context[size];
        collectContexts(V_PREPROCESSOR, tu, contexts, 0);
        IASTPreprocessorStatement[] result = new IASTPreprocessorStatement[size];
        for (int i = 0; i < size; ++i) 
            result[i] = createPreprocessorStatement(contexts[i]);

        return result;
    }

    private IASTPreprocessorStatement createPreprocessorStatement(
            _Context context) {
        IASTPreprocessorStatement result = null;
        if (context instanceof _Inclusion)
            result = createASTInclusion(((_Inclusion) context));
        else if (context instanceof _Include)
            result = createASTInclusion(((_Include) context));
        else if (context instanceof _MacroDefinition)
            result = createASTMacroDefinition((_MacroDefinition) context);
        else if (context instanceof _Undef)
            result = createASTUndef((_Undef) context);
        else if (context instanceof _Pragma)
            result = createASTPragma((_Pragma) context);
        else if (context instanceof _Error)
            result = createASTError((_Error) context);
        else if (context instanceof _Warning)
            result = createASTWarning((_Warning) context);
        else if (context instanceof _If)
            result = createASTIf((_If) context);
        else if (context instanceof _Ifdef)
            result = createASTIfdef((_Ifdef) context);
        else if (context instanceof _Ifndef)
            result = createASTIfndef((_Ifndef) context);
        else if (context instanceof _Else)
            result = createASTElse((_Else) context);
        else if (context instanceof _Elif)
            result = createASTElif((_Elif) context);
        else if (context instanceof _Endif)
            result = createASTEndif((_Endif) context);

        return result;
    }

    /**
     * @param endif
     * @return
     */
    private IASTPreprocessorStatement createASTEndif(_Endif endif) {
        IASTPreprocessorEndifStatement result = new ASTEndif();
        ((ASTNode) result).setOffsetAndLength(endif.context_directive_start, endif.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param elif
     * @return
     */
    private IASTPreprocessorStatement createASTElif(_Elif elif) {
    	IASTPreprocessorElifStatement result = new ASTElif(elif.taken, elif.condition);
        ((ASTNode) result).setOffsetAndLength(elif.context_directive_start, elif.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param else1
     * @return
     */
    private IASTPreprocessorStatement createASTElse(_Else e) {
        IASTPreprocessorElseStatement result = new ASTElse(e.taken);
        ((ASTNode) result).setOffsetAndLength(e.context_directive_start, e.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param ifndef
     * @return
     */
    private IASTPreprocessorStatement createASTIfndef(_Ifndef ifndef) {
        IASTPreprocessorIfndefStatement result = new ASTIfndef(ifndef.taken, ifndef.condition);
        ((ASTNode) result).setOffsetAndLength(ifndef.context_directive_start, ifndef.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param ifdef
     * @return
     */
    private IASTPreprocessorStatement createASTIfdef(_Ifdef ifdef) {
         IASTPreprocessorIfdefStatement result = new ASTIfdef(ifdef.taken, ifdef.condition);
        ((ASTNode) result).setOffsetAndLength(ifdef.context_directive_start, ifdef.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param if1
     * @return
     */
    private IASTPreprocessorStatement createASTIf(_If i) {
        IASTPreprocessorIfStatement result = new ASTIf(i.taken, i.condition);
        ((ASTNode) result).setOffsetAndLength(i.context_directive_start, i.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param error
     * @return
     */
    private IASTPreprocessorStatement createASTError(_Error error) {
        IASTPreprocessorErrorStatement result = new ASTError(error.msg);
        ((ASTNode) result).setOffsetAndLength(error.context_directive_start, error.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param warning
     * @return
     */
    private IASTPreprocessorStatement createASTWarning(_Warning warning) {
        IASTPreprocessorErrorStatement result = new ASTWarning(warning.msg);
        ((ASTNode) result).setOffsetAndLength(warning.context_directive_start, warning.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param pragma
     * @return
     */
    private IASTPreprocessorStatement createASTPragma(_Pragma pragma) {
        IASTPreprocessorPragmaStatement result = new ASTPragma(pragma.msg);
        ((ASTNode) result).setOffsetAndLength(pragma.getDirectiveStart(), pragma.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /**
     * @param undef
     * @return
     */
    private IASTPreprocessorStatement createASTUndef(_Undef undef) {
        IASTPreprocessorUndefStatement result = new ASTUndef(undef.getName());
        ((ASTNode) result).setOffsetAndLength(undef.getDirectiveStart(), undef.getDirectiveLength());
		result.setParent(rootNode);
		result.setPropertyInParent(IASTTranslationUnit.PREPROCESSOR_STATEMENT);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getLocations(int,
     *      int)
     */
    public IASTNodeLocation[] getLocations(int offset, int length) {
        if (tu == null)
            return EMPTY_LOCATION_ARRAY;
        _Context c = findContextForOffset(offset);
        if( c == null )
            return EMPTY_LOCATION_ARRAY;
        if (offset + length <= c.context_ends) {
            if (c instanceof _CompositeContext) {
                _Context[] subz = ((_CompositeContext) c).getSubContexts();
            	boolean foundNested = findNested( subz, offset, length);
                if (!foundNested)
                    return createSoleLocationArray(c, offset, length);
            } else
                return createSoleLocationArray(c, offset, length);
        }
        // check to see if we spill over into other
        _WeightedContext[] extraContexts = findAllContextsForLength(offset,
                length);
        if (extraContexts.length == 0)
            return EMPTY_LOCATION_ARRAY;
        if (extraContexts.length == 1)
            return createSoleLocationArray(extraContexts[0].context, offset,
                    length);

        return createMegaLocationArray(offset, length, extraContexts);
    }

    private boolean findNested(_Context[] subz, int offset, int length){
    	final int offset2= offset+length-1;
    	int left = 0;
    	int right = subz.length-1;
    	int middle = 0;
    	_Context sub = null;
    	
    	while( left <= right ){
    		middle = (left+right)/2;
    		sub = subz[middle];
    		if (offset2 < sub.context_directive_start) {
    			right= middle-1;
    		}
    		else if (offset >= sub.context_ends) {
    			left= middle+1;
    		} 
    		else {
    			return true;
    		}
    	}
    	
    	return false;
    }

    private IASTNodeLocation[] createMegaLocationArray(int offset, int length,
            _WeightedContext[] contexts) {
        IASTNodeLocation[] result = new IASTNodeLocation[contexts.length];
        int currentOffset = offset;
        for (int i = 0; i < contexts.length; ++i) {
            final IASTNodeLocation location = createSoleLocation(
                    contexts[i].context, currentOffset, contexts[i].count);
            result[i] = location;
            currentOffset += contexts[i].count;
        }
        return (IASTNodeLocation[]) ArrayUtil.removeNulls( IASTNodeLocation.class, result );
    }

    protected static final class _WeightedContext {
        public final _Context context;

        public final int count;

        public _WeightedContext(_Context c, int currentCount) {
            context = c;
            count = currentCount;
        }

    }

    protected _WeightedContext[] findAllContextsForLength(int offset, int length) {
        _WeightedContext[] result = new _WeightedContext[2];
        _Context cc = null;
        int currentCount = 0;
        length= Math.min(tu.context_ends, length);
        for (int i = offset; i < offset + length; ++i) {
            _Context r = tu.findContextContainingOffset(i);
            if (cc == null) {
                cc = r;
                currentCount = 1;
            } else if (cc == r)
                ++currentCount;
            else if (cc instanceof _MacroExpansion && r.hasAncestor(cc))
                ++currentCount;
            else {
                result = (_WeightedContext[]) ArrayUtil.append(_WeightedContext.class, result, 
                		new _WeightedContext(cc, currentCount));
                cc = r;
                currentCount = 1;
            }
        }
        result = (_WeightedContext[]) ArrayUtil.append(_WeightedContext.class, result,
        		new _WeightedContext(cc, currentCount));
        return (_WeightedContext[]) ArrayUtil.trim(_WeightedContext.class, result);
    }

    protected IASTNodeLocation createSoleLocation(_Context c, int offset,
            int length) {
    	
    	if (c instanceof _IPreprocessorDirective || c instanceof _Problem ) {
            if (c.containsInDirective(offset, length)) {
                _CompositeContext parent = c.parent;
                if (parent instanceof _CompositeFileContext) {
                	_CompositeFileContext fc = (_CompositeFileContext) parent;
                	return new FileLocation(fc.reader.filename, fc.getOffsetInContext(offset), length);
                }
            }
        }
        if (c instanceof _CompositeFileContext) {
            _CompositeFileContext cfc = (_CompositeFileContext) c;
			return new FileLocation(cfc.reader.filename, cfc.getOffsetInContext(offset), length);
        }
        if (c instanceof _MacroExpansion) {
            _MacroExpansion expansion = (_MacroExpansion) c;
            if (c.containsInDirective(offset, length)) {
                _CompositeContext parent = c.parent;
                if (parent instanceof _CompositeFileContext) {
                	_CompositeFileContext fc = (_CompositeFileContext) parent;
                	return new FileLocation(fc.reader.filename, fc.getOffsetInContext(offset), length);
                }
            }
            
            
            IASTNodeLocation[] locations = createSoleLocationArray(c.parent, c.getDirectiveStart(), c.getDirectiveLength());
            IASTPreprocessorMacroDefinition definition = null;
            _MacroDefinition d = (_MacroDefinition) expansion.definition;
            if (d.astNode != null)
                definition = d.astNode;
            else {
                IASTPreprocessorMacroDefinition astNode = createASTMacroDefinition(d);
                d.astNode = astNode;
                definition = astNode;
            }

            if (c instanceof _FunctionMacroExpansion) {
				_FunctionMacroExpansion fe = (_FunctionMacroExpansion) c;
				return new FunctionMacroExpansionLocation(definition, locations, fe.getOffsetInContext(offset), length, fe.actArgs);
			}else {
				_MacroExpansion me = (_MacroExpansion) c;
				return new MacroExpansionLocation(definition, locations,
						me.getOffsetInContext(offset), length);
			}
        }
        return null;
    }


    /**
     * @param c
     * @param offset
     * @param length
     * @return
     */
    protected IASTNodeLocation[] createSoleLocationArray(_Context c,
            int offset, int length) {
        IASTNodeLocation value = createSoleLocation(c, offset, length);
        if (value == null)
            return EMPTY_LOCATION_ARRAY;
        IASTNodeLocation[] result = new IASTNodeLocation[1];
        result[0] = value;
        return result;
    }

    protected _Context findContextForOffset(int offset) {
        return tu.findContextContainingOffset(offset);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#startTranslationUnit()
     */
    public void startTranslationUnit(CodeReader tu_reader) {
        tu.reader = tu_reader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endTranslationUnit(int)
     */
    public void endTranslationUnit(int offset) {
        tu.context_ends = offset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#startInclusion(char[],
     *      int)
     */
    public void startInclusion(CodeReader reader, int offset, int endOffset, int nameOffset, int nameEndoffset, char[] name, boolean systemInclude) {
        _Inclusion i = new _Inclusion(currentContext, reader, offset, endOffset, nameOffset, nameEndoffset, name, systemInclude);
        currentContext.addSubContext(i);
        currentContext = i;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endInclusion(char[],
     *      int)
     */
    public void endInclusion(CodeReader reader, int offset) {
        if( currentContext instanceof _Inclusion && 
                ((_Inclusion)currentContext).reader == reader )
        {
            ((_Inclusion) currentContext).context_ends = offset;
            currentContext = currentContext.getParent();
            return;
        }
        
        _CompositeContext test = currentContext;
        while( ( test = test.getParent() ) != tu )
        {
            if( test instanceof _Inclusion && 
                    ((_Inclusion)test).reader == reader )
            {
                currentContext = test;
                ((_Inclusion) currentContext).context_ends = offset;
                currentContext = currentContext.getParent();
                return;
            }            
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#enterObjectStyleMacroExpansion(char[],
     *      char[], int)
     */
    public void startObjectStyleMacroExpansion(IMacroDefinition macro,
            int startOffset, int endOffset) {
        _ObjectMacroExpansion context = new _ObjectMacroExpansion(
                currentContext, startOffset, endOffset, macro);
        currentContext.addSubContext(context);
        currentContext = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#exitObjectStyleMacroExpansion(char[],
     *      int)
     */
    public void endObjectStyleMacroExpansion(IMacroDefinition macro, int offset) {
        if( currentContext instanceof _MacroExpansion && ((_MacroExpansion)currentContext).definition == macro )
        {
            ((_ObjectMacroExpansion) currentContext).context_ends = offset; 
            currentContext = currentContext.getParent();
            return;
        }

        _CompositeContext test = currentContext;
        while( ( test = test.getParent() ) != tu )
        {
            if( test instanceof _MacroExpansion && ((_MacroExpansion)test).definition == macro )
            {
                currentContext = test;
                ((_ObjectMacroExpansion) currentContext).context_ends = offset; 
                currentContext = currentContext.getParent();
                return;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#enterFunctionStyleExpansion(char[],
     *      char[][], char[], int)
     */
    public void startFunctionStyleExpansion(IMacroDefinition macro,
            char[][] parameters, int startOffset, int endOffset, char[][] actParameters) {
        _FunctionMacroExpansion context = new _FunctionMacroExpansion(
                currentContext, startOffset, endOffset, macro, parameters, actParameters);
        currentContext.addSubContext(context);
        currentContext = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#exitFunctionStyleExpansion(char[],
     *      int)
     */
    public void endFunctionStyleExpansion(IMacroDefinition macro, int offset) {
        if( currentContext instanceof _MacroExpansion && ((_MacroExpansion)currentContext).definition == macro )
        {
            ((_FunctionMacroExpansion) currentContext).context_ends = offset; 
            currentContext = currentContext.getParent();
            return;
        }

        _CompositeContext test = currentContext;
        while( ( test = test.getParent() ) != tu )
        {
            if( test instanceof _MacroExpansion && ((_MacroExpansion)test).definition == macro )
            {
                currentContext = test;
                ((_FunctionMacroExpansion) currentContext).context_ends = offset; 
                currentContext = currentContext.getParent();
                return;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#defineObjectStyleMacro(org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro,
     *      int, int, int, int)
     */
    public IMacroDefinition defineObjectStyleMacro(ObjectStyleMacro m,
            int startOffset, int nameOffset, int nameEndOffset, int endOffset) {
        _ObjectMacroDefinition objectMacroDefinition = new _ObjectMacroDefinition(
                currentContext, startOffset, endOffset, m.name, nameOffset,
                m.getExpansion());
        currentContext.addSubContext(objectMacroDefinition);
        return objectMacroDefinition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#defineFunctionStyleMacro(org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro,
     *      int, int, int, int)
     */
    public IMacroDefinition defineFunctionStyleMacro(FunctionStyleMacro m,
            int startOffset, int nameOffset, int nameEndOffset, int endOffset) {
        final _FunctionMacroDefinition functionMacroDefinition = new _FunctionMacroDefinition(
                currentContext, startOffset, endOffset, m.name, nameOffset,
                m.getExpansion(), removeNullArguments(m.getOriginalParameters()));
        currentContext.addSubContext(functionMacroDefinition);
        return functionMacroDefinition;
    }

    /**
     * @param arglist
     * @return
     */
    private char[][] removeNullArguments(char[][] arglist) {
        int nullCount = 0;
        for (int i = 0; i < arglist.length; ++i)
            if (arglist[i] == null)
                ++nullCount;
        if (nullCount == 0)
            return arglist;
        char[][] old = arglist;
        int newSize = old.length - nullCount;
        arglist = new char[newSize][];
        for (int i = 0; i < newSize; ++i)
            arglist[i] = old[i];
        return arglist;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundIf(int,
     *      int)
     */
    public void encounterPoundIf(int startOffset, int endOffset, boolean taken, char[] condtion) {
        currentContext.addSubContext(new _If(currentContext, startOffset,
                endOffset, taken, condtion));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundPragma(int,
     *      int)
     */
    public void encounterPoundPragma(int startOffset, int endOffset, char[] msg) {
        currentContext.addSubContext(new _Pragma(currentContext, startOffset,
                endOffset, msg));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundError(int,
     *      int)
     */
    public void encounterPoundError(int startOffset, int endOffset, char[] msg) {
        currentContext.addSubContext(new _Error(currentContext, startOffset,
                endOffset, msg));
    }

    /*
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundWarning(int, int)
     */
    public void encounterPoundWarning(int startOffset, int endOffset, char[] msg) {
        currentContext.addSubContext(new _Warning(currentContext, startOffset,
                endOffset, msg));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundIfdef(int,
     *      int)
     */
    public void encounterPoundIfdef(int startOffset, int endOffset,
            boolean taken, char[] condition) {
        currentContext.addSubContext(new _Ifdef(currentContext, startOffset,
                endOffset, taken, condition));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundUndef(int,
     *      int)
     */
    public void encounterPoundUndef(int startOffset, int endOffset,
            char[] symbol, int nameOffset, IMacroDefinition macroDefinition) {
        currentContext.addSubContext(new _Undef(currentContext, startOffset,
                endOffset, symbol, nameOffset, macroDefinition));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundElse(int,
     *      int)
     */
    public void encounterPoundElse(int startOffset, int endOffset, boolean taken) {
        currentContext.addSubContext(new _Else(currentContext, startOffset,
                endOffset, taken));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundElif(int,
     *      int)
     */
    public void encounterPoundElif(int startOffset, int endOffset, boolean taken, char[] condition) {
        currentContext.addSubContext(new _Elif(currentContext, startOffset,
                endOffset, taken, condition));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundEndIf(int,
     *      int)
     */
    public void encounterPoundEndIf(int startOffset, int endOffset) {
        currentContext.addSubContext(new _Endif(currentContext, startOffset,
                endOffset));
    }

    /*
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundInclude(int, int, int, int, char[], boolean, boolean)
     */
    public void encounterPoundInclude(int startOffset, int nameOffset, int nameEndOffset, int endOffset, 
    		char[] name, boolean systemInclude, boolean active) {
    	encounterPoundInclude(startOffset, nameOffset, nameEndOffset, endOffset, name, null, systemInclude, active);
    }
    
    public void encounterPoundInclude(int startOffset, int nameOffset, int nameEndOffset, int endOffset, 
    		char[] name, char[] path, boolean systemInclude, boolean active) {
    	currentContext.addSubContext(new _Include(currentContext, startOffset, endOffset, nameOffset, nameEndOffset, name, path, systemInclude, active, path != null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getTranslationUnitPath()
     */
    public String getTranslationUnitPath() {
        return new String(tu.reader.filename);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#cleanup()
     */
    public void cleanup() {
        tu = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getScannerProblems()
     */
    public IASTProblem[] getScannerProblems() {
        int size = LocationMap.collectContexts(V_PROBLEMS, tu, null, 0);
        if (size == 0)
            return EMPTY_PROBLEMS_ARRAY;
        _Context[] contexts = new _Context[size];
        LocationMap.collectContexts(V_PROBLEMS, tu, contexts, 0);
        IASTProblem[] result = new IASTProblem[size];
        for (int i = 0; i < size; ++i) {
            result[i] = ((_Problem) contexts[i]).problem;
            result[i].setParent(rootNode);
            result[i].setPropertyInParent(IASTTranslationUnit.SCANNER_PROBLEM);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterIProblem(org.eclipse.cdt.core.parser.IProblem)
     */
    public void encounterProblem(IASTProblem problem) {
        ScannerASTProblem p = (ScannerASTProblem) problem;
        int offset= p.getOffset();
        int endoffset= offset + p.getLength();
        _Problem pr = new _Problem(currentContext, offset, endoffset, problem);
        currentContext.addSubContext(pr);
    }

    protected static final int V_ALL = 1;

    protected static final int V_INCLUSIONS = 2;

    protected static final int V_PROBLEMS = 3;

    protected static final int V_MACRODEFS = 4;

    protected static final int V_PREPROCESSOR = 5;

    protected static final int V_MACRODEFSUNDEFS = 6;

    protected static final int V_INCLUDE_DIRECTIVES = 7;
    
    private static final char[] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$
    private static final IASTName[] EMPTY_NAME_ARRAY = new IASTName[0];
    
    
    protected IASTTranslationUnit rootNode;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    

    protected static int collectContexts(int key, _Context source,
            _Context[] result, int s) {
        int startAt = s;
        int count = 0;
        switch (key) {
        case V_ALL:
            if (result != null)
                result[startAt++] = source;
            ++count;
            break;
        case V_INCLUSIONS:
            if (source instanceof _Inclusion) {
                if (result != null)
                    result[startAt++] = source;
                ++count;
            }
            break;
        case V_INCLUDE_DIRECTIVES:
            if (source instanceof _IPreprocessorIncludeDirective) {
                if (result != null)
                    result[startAt++] = source;
                ++count;
            }
            break;
        case V_PROBLEMS:
            if (source instanceof _Problem) {
                if (result != null)
                    result[startAt++] = source;
                ++count;
            }
            break;
        case V_MACRODEFS:
            if (source instanceof _MacroDefinition) {
                if (result != null)
                    result[startAt++] = source;
                ++count;
            }
            break;
        case V_PREPROCESSOR:
            if (source instanceof _IPreprocessorDirective) {
                if (result != null)
                    result[startAt++] = source;
                ++count;
            }
            break;
        case V_MACRODEFSUNDEFS:
            if (source instanceof _MacroDefinition || source instanceof _Undef) {
                if (result != null)
                    result[startAt++] = source;
                ++count;
            }
            break;
        }
        if (source instanceof _CompositeContext) {
            _Context[] l = ((_CompositeContext) source).getSubContexts();
            for (int i = 0; i < l.length; ++i) {
                int value = collectContexts(key, l[i], result, startAt);
                count += value;
                startAt += value;
            }
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getSignature(org.eclipse.cdt.core.dom.ast.IASTNodeLocation[])
     */
    public char[] getUnpreprocessedSignature(IASTNodeLocation[] locations) {

        switch (locations.length) {
        case 1:
            if (locations[0] instanceof IASTFileLocation) {
                IASTNodeLocation nodeLocation = locations[0];
                char[] name = ((IASTFileLocation) nodeLocation).getFileName()
                        .toCharArray();
                if (readerCompatable(nodeLocation, tu.reader, name))
                    return CharArrayUtils.extract(tu.reader.buffer,
                            nodeLocation.getNodeOffset(), nodeLocation
                                    .getNodeLength());

                int size = collectContexts(V_INCLUSIONS, tu, null, 0);
                if (size == 0)
                    return EMPTY_CHAR_ARRAY;
                _Context[] inclusions = new _Context[size];
                collectContexts(V_INCLUSIONS, tu, inclusions, 0);
                for (int i = 0; i < size; ++i) {
                    _Inclusion inc = (_Inclusion) inclusions[i];
                    if (readerCompatable(nodeLocation, inc.reader, name))
                        return CharArrayUtils.extract(inc.reader.buffer,
                                nodeLocation.getNodeOffset(), nodeLocation
                                        .getNodeLength());
                }
            }
            return EMPTY_CHAR_ARRAY;
        case 0:
            return EMPTY_CHAR_ARRAY;
        default:
            // TODO
            return EMPTY_CHAR_ARRAY;
        }
    }

    /**
     * @param nodeLocation
     * @param reader
     * @param name
     * @return
     */
    private boolean readerCompatable(IASTNodeLocation nodeLocation,
            CodeReader reader, char[] name) {
        if (!CharArrayUtils.equals(reader.filename, name))
            return false;
        if (nodeLocation.getNodeOffset() > reader.buffer.length)
            return false;
        if (nodeLocation.getNodeOffset() + nodeLocation.getNodeLength() > reader.buffer.length)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundIfndef(int,
     *      int, boolean)
     */
    public void encounterPoundIfndef(int startOffset, int endOffset,
           boolean taken, char[] condition) {
        currentContext.addSubContext(new _Ifndef(currentContext, startOffset,
                 endOffset, taken, condition));
    }

    _Inclusion findInclusion(_CompositeContext context, String path) {
        _Inclusion foundContext = null;
        _Context[] contexts = context.getSubContexts();
        _Inclusion tempContext= null;
        for (int i = 0; foundContext == null && i < contexts.length; i++) {
            if (contexts[i] instanceof _Inclusion) {
                tempContext = (_Inclusion) contexts[i];

                // check if the file matches the #include
                if (CharArrayUtils.equals(
                        tempContext.reader.filename, path
                                .toCharArray())) {
                    foundContext = tempContext;
                    break;
                }
                foundContext = findInclusion(tempContext, path);
            }
        }

        return foundContext;
    }

    private ASTPreprocessorSelectionResult getPreprocessorNode(
            int globalOffset, int length, _Context startContext)
            throws InvalidPreprocessorNodeException {
        IASTNode result = null;
        if (!(startContext instanceof _CompositeContext))
            throw new InvalidPreprocessorNodeException(NOT_VALID_MACRO,
                    globalOffset);
        _Context[] contexts = ((_CompositeContext) startContext).getSubContexts();

        // check if a macro in the location map is the selection
        for (int i = 0; result == null && i < contexts.length; i++) {
            _Context context = contexts[i];

            if (globalOffset < context.getDirectiveStart()) {
            	break;
            }
            
            // check if the _Context is the selection
            if (globalOffset == context.getDirectiveStart()) {
            	if( context instanceof _MacroExpansion) {
                    _MacroExpansion expansion = (_MacroExpansion)context;
                    if( expansion.definition.getName().length == length ) 
                        result = expansion.getName();
                }
            	else if (length == context.getDirectiveLength()) {
            		result = createPreprocessorStatement(context);
            	}
            }

            // check if a sub node of the macro is the selection // TODO
            // determine how this can be kept in sync with logic in
            // getAllPreprocessorStatements (i.e. 1:1 mapping)
            if (context.containsInDirective(globalOffset, length)) {
            	if (context instanceof _MacroDefinition) {
            		_MacroDefinition macroDefinition = (_MacroDefinition) context;
					if (globalOffset == macroDefinition.nameOffset
            				&& length == macroDefinition.name.length) {
            			result = createASTMacroDefinition(macroDefinition).getName();
					}
            	}
            }

            // if offset is past the _Context then increment globalOffset
            if (globalOffset >= context.getContextStart()) {
                globalOffset += context.getContextLength();
            }
        }

        return new ASTPreprocessorSelectionResult(result, globalOffset);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getPreprocessorNode(int,
     *      int)
     */
    public ASTPreprocessorSelectionResult getPreprocessorNode(String path,
            int offset, int length) throws InvalidPreprocessorNodeException {
        ASTPreprocessorSelectionResult result = null;

        int globalOffset = 0;
        _Context foundContext = tu;

        // is the selection in TU or an #include else it's an exception
        if (CharArrayUtils.equals(tu.reader.filename, path.toCharArray())) {
            globalOffset = offset; // in TU so start at the real offset
        } else {
            foundContext = findInclusion(tu, path);

            if (foundContext == null) {
                throw new InvalidPreprocessorNodeException(TU_INCLUDE_NOT_FOUND, globalOffset);
            } else if (foundContext instanceof _Inclusion) {
                globalOffset = foundContext.getContextStart() + offset; 
            }
        }

        result = getPreprocessorNode(globalOffset, length, foundContext);

        return result;
    }

    public void setRootNode(IASTTranslationUnit root) {
        this.rootNode = root;
    }

    public IASTFileLocation flattenLocations(IASTNodeLocation[] nodeLocations) {
        if (nodeLocations == null)
            return null;
        if (nodeLocations.length == 0)
            return null;
        if (nodeLocations.length == 1
                && nodeLocations[0] instanceof IASTFileLocation)
            return (IASTFileLocation) nodeLocations[0];
        IASTFileLocation[] result = new IASTFileLocation[nodeLocations.length];
        for (int i = 0; i < nodeLocations.length; ++i) {
            if (nodeLocations[i] != null)
                result[i] = nodeLocations[i].asFileLocation();
        }
        return flatten(result);
    }

    private IASTFileLocation flatten(IASTFileLocation[] result) {
        String filename = null;
        int offset = 0, length = 0;
        for (int i = 0; i < result.length; ++i) {
            if (i == 0) {
                offset = result[0].getNodeOffset();
                filename = result[0].getFileName();
                length = result[0].getNodeLength();
            } else {
                if( result[i] != null && !result[i].getFileName().equals( filename ) )
                    return null;
                if( result[i].getNodeOffset()  == result[i-1].getNodeOffset() )
                    continue;
                if (result[i] != null
                        && result[i].getNodeOffset() != (offset + length)  )
                    return null;
                if (result[i] != null)
                    length += result[i].getNodeLength();
            }
        }
        return new FileLocation(filename.toCharArray(), offset, length);
    }

    public IASTName[] getReferences(IMacroBinding binding) {
        if( binding instanceof MacroBinding )
        {
            IMacroDefinition d = ((MacroBinding)binding).getDefinition();
            _Context [] r = findReferences( tu, d );
            return createNameArray( r );
        }
        return EMPTY_NAME_ARRAY;
    }

    private IASTName[] createNameArray(_Context[] r) {
        IASTName [] result = new IASTName[ r.length ];
        for( int i = 0; i < r.length; ++i )
        {
            IASTName n = null;
            if( r[i] instanceof _MacroExpansion )
            {
                n = ((_MacroExpansion)r[i]).getName();
            }
            else if( r[i] instanceof _Undef )
            {
                n = ((_Undef)r[i]).getName();
                createASTUndef( (_Undef) r[i] );
            }
            result[i] = n;
        }
        return result;
    }

    protected _Context [] findReferences(_CompositeContext c, IMacroDefinition d) {
        _Context [] results = new _Context[2];
        _Context [] subs = c.getSubContexts();
        for( int i = 0; i < subs.length; ++i )
        {
            if( subs[i] instanceof _MacroExpansion )
            {
                if( ((_MacroExpansion)subs[i]).definition == d )
                    results = (_Context[]) ArrayUtil.append( _Context.class, results, subs[i] );
            }
            else if( subs[i] instanceof _Undef )
            {
                if( ((_Undef)subs[i]).macroDefn == d )
                    results = (_Context[]) ArrayUtil.append( _Context.class, results, subs[i] );
            }
            
            if( subs[i] instanceof _CompositeContext )
            {
                _Context [] s = findReferences(  (_CompositeContext) subs[i], d );
                if( s.length > 0 )
                    results = (_Context[]) ArrayUtil.addAll( _Context.class, results, s );
            }
        }
        return (_Context[]) ArrayUtil.trim( _Context.class, results );
    }

    public IASTName[] getDeclarations(IMacroBinding binding) {
        if( binding instanceof MacroBinding )
        {
            IMacroDefinition d = ((MacroBinding)binding).getDefinition();
            if( d instanceof _MacroDefinition )
                return createNameArray( ((_MacroDefinition)d) );
        }
        return EMPTY_NAME_ARRAY;
    }

    private IASTName[] createNameArray(_MacroDefinition definition) {
        if( definition == null ) return EMPTY_NAME_ARRAY;
        IASTName [] result = new IASTName[1];
        if( definition.astNode == null )
        {
            IASTPreprocessorMacroDefinition astNode = createASTMacroDefinition(definition);
            definition.astNode = astNode;
            result[0] = definition.astNode.getName();
        }
        else
        {
            result[0] = definition.astNode.getName();
        }
        return result;
    }

    public IASTName[] getMacroExpansions() {
        // TODO Auto-generated method stub
        return null;
    }

    public IDependencyTree getDependencyTree() {
        DependencyTree result = new DependencyTree(getTranslationUnitPath());
        buildDependencyTree( result, tu );
        return result;
    }

    protected void buildDependencyTree(IDependencyNodeHost result, _CompositeFileContext context) {
        _Context [] subs = context.getSubContexts();
        for( int i = 0; i < subs.length; ++i )
        {
            if( subs[i] instanceof _Inclusion )
            {
                IASTTranslationUnit.IDependencyTree.IASTInclusionNode node = createDepTreeNode( (_Inclusion)subs[i] );
                result.addInclusionNode( node );
            }
        }
    }

    private IASTInclusionNode createDepTreeNode(_Inclusion inclusion) {
        IASTPreprocessorIncludeStatement stmt = createASTInclusion( inclusion );
        InclusionNode node = new  InclusionNode( stmt );
        buildDependencyTree(node, inclusion);
        return node;
    }

    public IMacroDefinition registerBuiltinObjectStyleMacro(ObjectStyleMacro macro) {
        IMacroDefinition result = new _ObjectMacroDefinition( tu, -1, -1, macro.name, -1, macro );
        tu.addBuiltinMacro( result );
        return result;
    }

    public IMacroDefinition registerBuiltinFunctionStyleMacro(FunctionStyleMacro macro) {
        IMacroDefinition result = new _FunctionMacroDefinition( tu, -1, -1, macro.name, -1, macro, removeNullArguments( macro.arglist ) );
        tu.addBuiltinMacro( result );
        return result;
    }

    public IMacroDefinition registerBuiltinDynamicFunctionStyleMacro(DynamicFunctionStyleMacro macro) {
        IMacroDefinition result = new _MacroDefinition( tu, -1, -1, macro.name, -1, macro.getExpansion() );
        tu.addBuiltinMacro( result );
        return result;
    }

    public IMacroDefinition registerBuiltinDynamicStyleMacro(DynamicStyleMacro macro) {
        IMacroDefinition result = new _MacroDefinition( tu, -1, -1, macro.name, -1, macro.execute() );
        tu.addBuiltinMacro( result );
        return result;
    }

	public String getContainingFilename(int offset) {
		_Context c = findContextForOffset(offset);
		if( c == null ) return EMPTY_STRING;
		if (c instanceof _Inclusion && c.containsInDirective(offset, 1)) {
			c = c.getParent();
		}
		_CompositeFileContext file = c.getContainingFileContext();
		if( file == null ) return EMPTY_STRING;
		return file.reader.getPath();
	}
}

/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
/*
 * Created on Mar 31, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType.CPPClassTypeProblem;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author aniefer
 */
public class CPPClassTemplate extends CPPTemplateDefinition implements
		ICPPClassTemplate, ICPPClassType, ICPPInternalClassType, ICPPInternalClassTemplate {
	
    public static class CPPClassTemplateDelegate extends CPPClassType.CPPClassTypeDelegate implements ICPPClassTemplate, ICPPInternalClassTemplate {
        public CPPClassTemplateDelegate( IASTName name, ICPPClassType cls ) {
            super( name, cls );
        }
        public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
            return ((ICPPClassTemplate)getBinding()).getPartialSpecializations();
        }
        public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
            return ((ICPPClassTemplate)getBinding()).getTemplateParameters();
        }
        public void addSpecialization( IType[] arguments, ICPPSpecialization specialization ) {
            final IBinding binding = getBinding();
            if (binding instanceof ICPPInternalBinding) {
            	((ICPPInternalTemplate)binding).addSpecialization( arguments, specialization );
            }
        }
        public IBinding instantiate( IType[] arguments ) {
            return ((ICPPInternalTemplateInstantiator)getBinding()).instantiate( arguments );
        }
        public ICPPSpecialization deferredInstance( IType[] arguments ) {
            return ((ICPPInternalTemplateInstantiator)getBinding()).deferredInstance( arguments );
        }
        public ICPPSpecialization getInstance( IType[] arguments ) {
            return ((ICPPInternalTemplateInstantiator)getBinding()).getInstance( arguments );
        }
		public void addPartialSpecialization( ICPPClassTemplatePartialSpecialization spec ) {
            final IBinding binding = getBinding();
            if (binding instanceof ICPPInternalClassTemplate) {
            	((ICPPInternalClassTemplate)getBinding()).addPartialSpecialization( spec );
            }
		}
    }
	private ICPPClassTemplatePartialSpecialization [] partialSpecializations = null;
	
	private class FindDefinitionAction extends CPPASTVisitor {
	    private char [] nameArray = CPPClassTemplate.this.getNameCharArray();
	    public IASTName result = null;
	    
	    {
	        shouldVisitNames          = true;
			shouldVisitDeclarations   = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarators    = true;
	    }
	    
	    public int visit( IASTName name ){
			if( name instanceof ICPPASTTemplateId || name instanceof ICPPASTQualifiedName )
				return PROCESS_CONTINUE;
			char [] c = name.toCharArray();
			if( name.getParent() instanceof ICPPASTTemplateId )
				name = (IASTName) name.getParent();
			if( name.getParent() instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)name.getParent()).getNames();
				if( ns[ ns.length - 1 ] != name )
					return PROCESS_CONTINUE;
				name = (IASTName) name.getParent();
			}
			
	        if( name.getParent() instanceof ICPPASTCompositeTypeSpecifier &&
	            CharArrayUtils.equals( c, nameArray ) ) 
	        {
	            IBinding binding = name.resolveBinding();
	            if( binding == CPPClassTemplate.this ){
	            	if( name instanceof ICPPASTQualifiedName ){
	            		IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	            		name = ns[ ns.length - 1 ];
	            	}
	                result = name;
	                return PROCESS_ABORT;
	            }
	        }
	        return PROCESS_CONTINUE; 
	    }
	    
		public int visit( IASTDeclaration declaration ){ 
		    if(declaration instanceof IASTSimpleDeclaration || declaration instanceof ICPPASTTemplateDeclaration )
				return PROCESS_CONTINUE;
			return PROCESS_SKIP; 
		}
		public int visit( IASTDeclSpecifier declSpec ){
		    return (declSpec instanceof ICPPASTCompositeTypeSpecifier ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		public int visit( IASTDeclarator declarator ) 			{ return PROCESS_SKIP; }
	}
	/**
	 * @param decl
	 */
	public CPPClassTemplate(IASTName name) {
		super(name);
	}

	public ICPPSpecialization deferredInstance( IType [] arguments ){
		ICPPSpecialization instance = getInstance( arguments );
		if( instance == null ){
			instance = new CPPDeferredClassInstance( this, arguments );
			addSpecialization( arguments, instance );
		}
		return instance;
	}
	
	private void checkForDefinition(){
		FindDefinitionAction action = new FindDefinitionAction();
		IASTNode node = CPPVisitor.getContainingBlockItem( declarations[0] ).getParent();
		while( node instanceof ICPPASTTemplateDeclaration )
			node = node.getParent();
		node.accept( action );
	    definition = action.result;
		
		if( definition == null ){
			node.getTranslationUnit().accept( action );
		    definition = action.result;
		}
		
		return;
	}
	
	public void addPartialSpecialization( ICPPClassTemplatePartialSpecialization spec ){
		partialSpecializations = (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.append( ICPPClassTemplatePartialSpecialization.class, partialSpecializations, spec );
	}
	
	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(){
	    if( definition != null ){
	    	IASTNode node = definition.getParent();
	    	if( node instanceof ICPPASTQualifiedName )
	    		node = node.getParent();
	    	if( node instanceof ICPPASTCompositeTypeSpecifier )
	    		return (ICPPASTCompositeTypeSpecifier) node;
	    }
	    return null;
	}
	/**
	 * @param templateParameter
	 * @return
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase [] getBases() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTName node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPBase [] { new CPPBaseClause.CPPBaseProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
		ICPPASTBaseSpecifier [] bases = getCompositeTypeSpecifier().getBaseSpecifiers();
		if( bases.length == 0 )
		    return ICPPBase.EMPTY_BASE_ARRAY;
		
		ICPPBase [] bindings = new ICPPBase[ bases.length ];
		for( int i = 0; i < bases.length; i++ ){
		    bindings[i] = new CPPBaseClause( bases[i] );
		}
		
		return bindings; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
	    if( definition == null ){
	        checkForDefinition();
	        if( definition == null ){
	            IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
	            return new IField [] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        }
	    }

		IField[] fields = getDeclaredFields();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			IBinding b = bases[i].getBaseClass();
			if( b instanceof ICPPClassType )
				fields = (IField[]) ArrayUtil.addAll( IField.class, fields, ((ICPPClassType)b).getFields() );
        }
		return (IField[]) ArrayUtil.trim( IField.class, fields );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) throws DOMException {
		IBinding [] bindings = CPPSemantics.findBindings( getCompositeScope(), name, true );
		IField field = null;
		for ( int i = 0; i < bindings.length; i++ ) {
            if( bindings[i] instanceof IField ){
                if( field == null )
                    field = (IField) bindings[i];
                else {
                    IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                    return new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray() );
                }
            }
        }
		return field;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public ICPPField[] getDeclaredFields() throws DOMException {
	    if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPField[] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPField [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
            if( decls[i] instanceof IASTSimpleDeclaration ){
                IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
                for ( int j = 0; j < dtors.length; j++ ) {
                    binding = dtors[j].getName().resolveBinding();
                    if( binding instanceof ICPPField )
                        result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
                }
            } else if( decls[i] instanceof ICPPASTUsingDeclaration ){
                IASTName n = ((ICPPASTUsingDeclaration)decls[i]).getName();
                binding = n.resolveBinding();
                if( binding instanceof ICPPUsingDeclaration ){
                    IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
                    for ( int j = 0; j < bs.length; j++ ) {
                        if( bs[j] instanceof ICPPField )
                            result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, bs[j] );
                    }
                } else if( binding instanceof ICPPField ) {
                    result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
                }
            }
        }
		return (ICPPField[]) ArrayUtil.trim( ICPPField.class, result );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() throws DOMException {
		ObjectSet set = new ObjectSet(4);
		set.addAll( getDeclaredMethods() );
		ICPPClassScope scope = (ICPPClassScope) getCompositeScope();
		set.addAll( scope.getImplicitMethods() );
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			IBinding b = bases[i].getBaseClass();
			if( b instanceof ICPPClassType )
				set.addAll( ((ICPPClassType)b).getMethods() );
        }
		return (ICPPMethod[]) set.keyArray( ICPPMethod.class );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		if( definition == null ){
	        checkForDefinition();
	        if( definition == null ){
	            IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
	            return new ICPPMethod [] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        }
	    }

		ICPPMethod[] methods = getDeclaredMethods();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			IBinding b = bases[i].getBaseClass();
			if( b instanceof ICPPClassType )
				methods = (ICPPMethod[]) ArrayUtil.addAll( ICPPMethod.class, methods, ((ICPPClassType)b).getAllDeclaredMethods() );
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, methods );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
	    if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPMethod[] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPMethod [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
			IASTDeclaration decl = decls[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
            if( decl instanceof IASTSimpleDeclaration ){
                IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
                for ( int j = 0; j < dtors.length; j++ ) {
                    binding = dtors[j].getName().resolveBinding();
                    if( binding instanceof ICPPMethod)
                        result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            } else if( decl instanceof IASTFunctionDefinition ){
                IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
                dtor = CPPVisitor.getMostNestedDeclarator( dtor );
                binding = dtor.getName().resolveBinding();
                if( binding instanceof ICPPMethod ){
                    result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            } else if( decl instanceof ICPPASTUsingDeclaration ){
                IASTName n = ((ICPPASTUsingDeclaration)decl).getName();
                binding = n.resolveBinding();
                if( binding instanceof ICPPUsingDeclaration ){
                    IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
                    for ( int j = 0; j < bs.length; j++ ) {
                        if( bs[j] instanceof ICPPMethod )
                            result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, bs[j] );
                    }
                } else if( binding instanceof ICPPMethod ) {
                    result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            }
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, result );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	public ICPPConstructor[] getConstructors() throws DOMException {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPConstructor [] { new CPPConstructor.CPPConstructorProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
        
        ICPPClassScope scope = (ICPPClassScope) getCompositeScope();
        if( ASTInternal.isFullyCached(scope))
        	return ((CPPClassScope)scope).getConstructors( true );
        	
        IASTDeclaration [] members = getCompositeTypeSpecifier().getMembers();
        for( int i = 0; i < members.length; i++ ){
        	IASTDeclaration decl = members[i];
        	if( decl instanceof ICPPASTTemplateDeclaration )
        		decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			if( decl instanceof IASTSimpleDeclaration ){
			    IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
			    for( int j = 0; j < dtors.length; j++ ){
			        if( dtors[j] == null ) break;
		            ASTInternal.addName(scope,  dtors[j].getName() );
			    }
			} else if( decl instanceof IASTFunctionDefinition ){
			    IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
			    ASTInternal.addName(scope,  dtor.getName() );
			}
        }
        
        return ((CPPClassScope)scope).getConstructors( true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	public IBinding[] getFriends() {
		//TODO
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
	    if( definition != null ) {
	    	ICPPASTCompositeTypeSpecifier cts= getCompositeTypeSpecifier();
	    	if (cts != null) {
	    		return cts.getKey();
	    	}
	    	IASTNode n= definition.getParent();
	    	if (n instanceof ICPPASTElaboratedTypeSpecifier) {
	    		return ((ICPPASTElaboratedTypeSpecifier)n).getKind();
	    	}
	    }
	    
	    if( declarations != null && declarations.length > 0 ){
	        IASTNode n = declarations[0].getParent();
	        if( n instanceof ICPPASTElaboratedTypeSpecifier ){
	            return ((ICPPASTElaboratedTypeSpecifier)n).getKind();
	        }
	    }
	     
		return ICPPASTElaboratedTypeSpecifier.k_class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
	    if( definition == null )
	        checkForDefinition();
		if( definition != null ) {
			IASTNode parent = definition.getParent();
			while (parent instanceof IASTName)
				parent = parent.getParent();
			if (parent instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier)parent;
				return compSpec.getScope();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassType#getConversionOperators()
	 */
	public ICPPMethod[] getConversionOperators() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef || type instanceof IIndexType)
            return type.isSameType( this );
        return false;
    }

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		partialSpecializations = (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.trim( ICPPClassTemplatePartialSpecialization.class, partialSpecializations );
		return partialSpecializations;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPClassTemplateDelegate( name, this );
    }

	public ICPPClassType[] getNestedClasses() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPClassType[] { new CPPClassTypeProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPClassType [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
			IASTDeclaration decl = decls[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
            if( decl instanceof IASTSimpleDeclaration ){
				IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
				if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
					binding = ((ICPPASTCompositeTypeSpecifier)declSpec).getName().resolveBinding();
				} else if( declSpec instanceof ICPPASTElaboratedTypeSpecifier &&
						   ((IASTSimpleDeclaration)decl).getDeclarators().length == 0 )
				{
					binding = ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding();
				}
				if( binding instanceof ICPPClassType )
					result = (ICPPClassType[])ArrayUtil.append( ICPPClassType.class, result, binding );
            } 
        }
		return (ICPPClassType[]) ArrayUtil.trim( ICPPClassType.class, result );
	}
}

package cloud.goudai.httpclient.processor.internal.model.common;


import com.google.auto.common.MoreTypes;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

import static cloud.goudai.httpclient.processor.internal.utils.Utils.getQualifiedName;


/**
 * @author jianglin
 * @date 2019/11/28
 */
public class TypeFactory {

    private final Elements elementUtils;
    private final Types typeUtils;
    private final Messager messager;

    private final TypeMirror iterableType;
    private final TypeMirror collectionType;
    private final TypeMirror mapType;
    private final TypeMirror streamType;

    private static final Map<TypeKind, String> TYPE_KIND_NAME = new EnumMap<>(TypeKind.class);

    static {
        TYPE_KIND_NAME.put(TypeKind.BOOLEAN, "boolean");
        TYPE_KIND_NAME.put(TypeKind.BYTE, "byte");
        TYPE_KIND_NAME.put(TypeKind.SHORT, "short");
        TYPE_KIND_NAME.put(TypeKind.INT, "int");
        TYPE_KIND_NAME.put(TypeKind.LONG, "long");
        TYPE_KIND_NAME.put(TypeKind.CHAR, "char");
        TYPE_KIND_NAME.put(TypeKind.FLOAT, "float");
        TYPE_KIND_NAME.put(TypeKind.DOUBLE, "double");
    }

    public TypeFactory(Elements elementUtils, Types typeUtils,
                       Messager messager, Map<String, String> notToBeImportedTypes) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.messager = messager;

        iterableType = typeUtils.erasure(elementUtils.getTypeElement(Iterable.class.getCanonicalName()).asType());
        collectionType =
                typeUtils.erasure(elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType());
        mapType = typeUtils.erasure(elementUtils.getTypeElement(Map.class.getCanonicalName()).asType());
        TypeElement streamTypeElement = elementUtils.getTypeElement("java.util.stream.Stream");
        streamType = streamTypeElement == null ? null : typeUtils.erasure(streamTypeElement.asType());
    }

    public Type getTypeForLiteral(Class<?> type) {
        return type.isPrimitive() ? getType(getPrimitiveType(type), true)
                : getType(type.getCanonicalName(), true);
    }

    public Type getType(Class<?> type) {
        return type.isPrimitive() ? getType(getPrimitiveType(type)) : getType(type.getCanonicalName());
    }

    public Type getType(String canonicalName) {
        return getType(canonicalName, false);
    }

    private Type getType(String canonicalName, boolean isLiteral) {
        TypeElement typeElement = elementUtils.getTypeElement(canonicalName);

        if (typeElement == null) {
            throw new AnnotationProcessingException(
                    "Couldn't find type " + canonicalName + ". Are you missing a dependency on your classpath?"
            );
        }

        return getType(typeElement, isLiteral);
    }


    /**
     * Determines if the type with the given full qualified name is part of the classpath
     *
     * @param canonicalName Name of the type to be checked for availability
     * @return true if the type with the given full qualified name is part of the classpath.
     */
    public boolean isTypeAvailable(String canonicalName) {
        return null != elementUtils.getTypeElement(canonicalName);
    }

    public Type getWrappedType(Type type) {
        Type result = type;
        if (type.isPrimitive()) {
            PrimitiveType typeMirror = (PrimitiveType) type.getTypeMirror();
            result = getType(typeUtils.boxedClass(typeMirror));
        }
        return result;
    }

    public Type getType(TypeElement typeElement) {
        return getType(typeElement.asType(), false);
    }

    private Type getType(TypeElement typeElement, boolean isLiteral) {
        return getType(typeElement.asType(), isLiteral);
    }

    public Type getType(TypeMirror mirror) {
        return getType(mirror, false);
    }

    private Type getType(TypeMirror mirror, boolean isLiteral) {


        boolean isIterableType = typeUtils.isSubtype(mirror, iterableType);
        boolean isCollectionType = typeUtils.isSubtype(mirror, collectionType);
        boolean isMapType = typeUtils.isSubtype(mirror, mapType);
        boolean isStreamType = streamType != null && typeUtils.isSubtype(mirror, streamType);

        boolean isEnumType;
        boolean isInterface;
        String name;
        String packageName;
        String qualifiedName;
        TypeElement typeElement;
        Type componentType;
        Boolean toBeImported = null;

        if (mirror.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) mirror;

            isEnumType = declaredType.asElement().getKind() == ElementKind.ENUM;
            isInterface = declaredType.asElement().getKind() == ElementKind.INTERFACE;
            name = declaredType.asElement().getSimpleName().toString();

            typeElement = (TypeElement) declaredType.asElement();

            if (typeElement != null) {
                packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                qualifiedName = typeElement.getQualifiedName().toString();
            } else {
                packageName = null;
                qualifiedName = name;
            }

            componentType = null;
        } else if (mirror.getKind() == TypeKind.ARRAY) {
            TypeMirror componentTypeMirror = getComponentType(mirror);
            StringBuilder builder = new StringBuilder("[]");

            while (componentTypeMirror.getKind() == TypeKind.ARRAY) {
                componentTypeMirror = getComponentType(componentTypeMirror);
                builder.append("[]");
            }

            if (componentTypeMirror.getKind() == TypeKind.DECLARED) {
                DeclaredType declaredType = (DeclaredType) componentTypeMirror;
                TypeElement componentTypeElement = (TypeElement) declaredType.asElement();

                String arraySuffix = builder.toString();
                name = componentTypeElement.getSimpleName().toString() + arraySuffix;
                packageName = elementUtils.getPackageOf(componentTypeElement).getQualifiedName().toString();
                qualifiedName = componentTypeElement.getQualifiedName().toString() + arraySuffix;
            } else if (componentTypeMirror.getKind().isPrimitive()) {
                // When the component type is primitive and is annotated with ElementType.TYPE_USE then
                // the typeMirror#toString returns (@CustomAnnotation :: byte) for the javac compiler
                name = getNativeTypeName(componentTypeMirror.getKind()) + builder.toString();
                packageName = null;
                // for primitive types only name (e.g. byte, short..) required as qualified name
                qualifiedName = name;
                toBeImported = false;
            } else {
                name = mirror.toString();
                packageName = null;
                qualifiedName = name;
                toBeImported = false;
            }

            isEnumType = false;
            isInterface = false;
            typeElement = null;
            componentType = getType(getComponentType(mirror));
        } else {
            isEnumType = false;
            isInterface = false;
            name = mirror.toString();
            packageName = null;
            qualifiedName = name;
            typeElement = null;
            componentType = null;
            toBeImported = false;
        }

        return new Type(
                typeUtils, elementUtils, this,
                mirror,
                typeElement,
                getTypeParameters(mirror, false),
                componentType,
                packageName,
                name,
                qualifiedName,
                isInterface,
                isEnumType,
                isIterableType,
                isCollectionType,
                isMapType,
                isStreamType,
                toBeImported,
                isLiteral);
    }

    private boolean canBeProcessed(TypeMirror type) {
        if (type.getKind() == TypeKind.ERROR) {
            return false;
        }

        if (type.getKind() != TypeKind.DECLARED) {
            return true;
        }
        return true;
    }

    /**
     * Returns the Type that represents the declared Class type of the given type. For primitive types, the boxed class
     * will be used. Examples:
     * <ul>
     * <li>If type represents {@code java.lang.Integer}, it will return the type that represents {@code Class<Integer>}.
     * </li>
     * <li>If type represents {@code int}, it will return the type that represents {@code Class<Integer>}.</li>
     * </ul>
     *
     * @param type the type to return the declared class type for
     * @return the type representing {@code Class<type>}.
     */
    public Type classTypeOf(Type type) {
        TypeMirror typeToUse;
        if (type.isVoid()) {
            return null;
        } else if (type.isPrimitive()) {
            typeToUse = typeUtils.boxedClass((PrimitiveType) type.getTypeMirror()).asType();
        } else {
            typeToUse = type.getTypeMirror();
        }

        return getType(typeUtils.getDeclaredType(elementUtils.getTypeElement("java.lang.Class"), typeToUse));
    }

    /**
     * Get the ExecutableType for given method as part of usedMapper. Possibly parameterized types in method declaration
     * will be evaluated to concrete types then.
     *
     * <b>IMPORTANT:</b> This should only be used from the Processors, as they are operating over executable elements.
     * The internals should not be using this function and should not be using the {@link ExecutableElement} directly.
     *
     * @param includingType the type on which's scope the method type shall be evaluated
     * @param method        the method
     * @return the ExecutableType representing the method as part of usedMapper
     */
    public ExecutableType getMethodType(DeclaredType includingType, ExecutableElement method) {
        TypeMirror asMemberOf = typeUtils.asMemberOf(includingType, method);
        return (ExecutableType) asMemberOf;
    }

    /**
     * Get the Type for given method as part of usedMapper. Possibly parameterized types in method declaration will be
     * evaluated to concrete types then.
     *
     * @param includingType the type on which's scope the method type shall be evaluated
     * @param method        the method
     * @return the ExecutableType representing the method as part of usedMapper
     */
    public TypeMirror getMethodType(DeclaredType includingType, Element method) {
        return typeUtils.asMemberOf(includingType, method);
    }


    public List<Parameter> getParameters(ExecutableType methodType, ExecutableElement method) {
        List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
        List<? extends VariableElement> parameters = method.getParameters();
        List<Parameter> result = new ArrayList<>(parameters.size());

        Iterator<? extends VariableElement> varIt = parameters.iterator();
        Iterator<? extends TypeMirror> typesIt = parameterTypes.iterator();
        int index = 0;
        while (varIt.hasNext()) {
            VariableElement parameter = varIt.next();
            TypeMirror parameterType = typesIt.next();

            Type type = getType(parameterType);

            // if the method has varargs and this is the last parameter
            // we know that this parameter should be used as varargs
            boolean isVarArgs = !varIt.hasNext() && method.isVarArgs();

            result.add(Parameter.forElementAndType(parameter, type, index++, isVarArgs));
        }

        return result;
    }

    private boolean isExecutableType(TypeMirror accessorType) {
        return accessorType.getKind() == TypeKind.EXECUTABLE;
    }

    public Type getReturnType(ExecutableType method) {
        return getType(method.getReturnType());
    }

    public List<Type> getThrownTypes(ExecutableType method) {
        return extractTypes(method.getThrownTypes());
    }

    private List<Type> extractTypes(List<? extends TypeMirror> typeMirrors) {
        Set<Type> types = new HashSet<>(typeMirrors.size());

        for (TypeMirror typeMirror : typeMirrors) {
            types.add(getType(typeMirror));
        }

        return new ArrayList<>(types);
    }

    private List<Type> getTypeParameters(TypeMirror mirror, boolean isImplementationType) {
        if (mirror.getKind() != TypeKind.DECLARED) {
            return java.util.Collections.emptyList();
        }

        DeclaredType declaredType = (DeclaredType) mirror;
        List<Type> typeParameters = new ArrayList<>(declaredType.getTypeArguments().size());

        for (TypeMirror typeParameter : declaredType.getTypeArguments()) {
            if (isImplementationType) {
                typeParameters.add(getType(typeParameter).getTypeBound());
            } else {
                typeParameters.add(getType(typeParameter));
            }
        }

        return typeParameters;
    }

    public List<Field> getFields(Type type) {
        List<Field> fields = new ArrayList<>();
        for (VariableElement element : type.getAllFields()) {
            Type varType = getType(element.asType());
            String fieldName = element.getSimpleName().toString();
            ExecutableElement readerMethod = getReader(type.getAllMethods(), fieldName, varType);
            if (readerMethod == null) {
                continue;
            }
            Field field = new Field(fieldName, varType, readerMethod);
            fields.add(field);
        }
        return fields;
    }

    private ExecutableElement getReader(List<ExecutableElement> methods, String fieldName, Type fieldType) {
        ExecutableElement readerMethod = null;
        for (ExecutableElement method : methods) {
            if (!method.getParameters().isEmpty()) {
                continue;
            }
            boolean sameType = MoreTypes.equivalence().equivalent(method.getReturnType(), fieldType
                    .getTypeMirror());
            String methodName = method.getSimpleName().toString();
            boolean isGetter = methodName.equalsIgnoreCase("get" + fieldName);

            boolean isBooleanGetterName = methodName.equalsIgnoreCase("is" + fieldName);
            boolean returnTypeIsBoolean = method.getReturnType().getKind() == TypeKind.BOOLEAN ||
                    "java.lang.Boolean".equals(getQualifiedName(method.getReturnType()));
            if (sameType && (isGetter || (isBooleanGetterName && returnTypeIsBoolean))) {
                readerMethod = method;
            }
        }
        return readerMethod;
    }

    private TypeMirror getPrimitiveType(Class<?> primitiveType) {
        return primitiveType == byte.class ? typeUtils.getPrimitiveType(TypeKind.BYTE) :
                primitiveType == short.class ? typeUtils.getPrimitiveType(TypeKind.SHORT) :
                        primitiveType == int.class ? typeUtils.getPrimitiveType(TypeKind.INT) :
                                primitiveType == long.class ? typeUtils.getPrimitiveType(TypeKind.LONG) :
                                        primitiveType == float.class ? typeUtils.getPrimitiveType(TypeKind.FLOAT) :
                                                primitiveType == double.class ?
                                                        typeUtils.getPrimitiveType(TypeKind.DOUBLE) :
                                                        primitiveType == boolean.class ?
                                                                typeUtils.getPrimitiveType(TypeKind.BOOLEAN) :
                                                                primitiveType == char.class ?
                                                                        typeUtils.getPrimitiveType(TypeKind.CHAR) :
                                                                        typeUtils.getPrimitiveType(TypeKind.VOID);
    }

    private TypeMirror getComponentType(TypeMirror mirror) {
        if (mirror.getKind() != TypeKind.ARRAY) {
            return null;
        }

        ArrayType arrayType = (ArrayType) mirror;
        return arrayType.getComponentType();
    }

    /**
     * creates a void return type
     *
     * @return void type
     */
    public Type createVoidType() {
        return getType(typeUtils.getNoType(TypeKind.VOID));
    }

    /**
     * Establishes the type bound:
     * <ol>
     * <li>{@code <? extends Number>}, returns Number</li>
     * <li>{@code <? super Number>}, returns Number</li>
     * <li>{@code <?>}, returns Object</li>
     * <li>{@code <T extends Number>, returns Number}</li>
     * </ol>
     *
     * @param typeMirror the type to return the bound for
     * @return the bound for this parameter
     */
    public TypeMirror getTypeBound(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.WILDCARD) {
            WildcardType wildCardType = (WildcardType) typeMirror;
            if (wildCardType.getExtendsBound() != null) {
                return wildCardType.getExtendsBound();
            }

            if (wildCardType.getSuperBound() != null) {
                return wildCardType.getSuperBound();
            }

            String wildCardName = wildCardType.toString();
            if ("?".equals(wildCardName)) {
                return elementUtils.getTypeElement(Object.class.getCanonicalName()).asType();
            }
        } else if (typeMirror.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariableType = (TypeVariable) typeMirror;
            if (typeVariableType.getUpperBound() != null) {
                return typeVariableType.getUpperBound();
            }
            // Lowerbounds intentionally left out: Type variables otherwise have a lower bound of NullType.
        }

        return typeMirror;
    }

    public static String getNativeTypeName(TypeKind typeKind) {
        return TYPE_KIND_NAME.get(typeKind);
    }

    public Messager getMessager() {
        return messager;
    }
}

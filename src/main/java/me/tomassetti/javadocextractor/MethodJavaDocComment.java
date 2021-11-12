package me.tomassetti.javadocextractor;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MethodJavaDocComment {
    String content;

    public String getMainContent() {
        return mainContent;
    }

    String mainContent;

    public String getReturnComment() {
        return returnComment;
    }

    String returnComment;
    static final String TAG_PARAM = "@param ";
    static final String TAG_RERURN = "@return ";
    static final String TAG_VALUE = "@value ";
    /**
     * 参数里,通过这个方法获取参数的className全限定名
     */
    public static final MyDataKey KEY_PARAM_CLASSNAME = new MyDataKey("my_className");
    /**
     * 参数里,通过这个getData(KEY_PARAM_VALUE)获取参数的写在注释上的值
     */
    public static final MyDataKey KEY_PARAM_VALUE = new MyDataKey("value_param");

    NodeList<ImportDeclaration> imports;

    public Map<String, String> getParamsComment() {
        return paramsComment;
    }

    Map<String,String> paramsComment = new HashMap<>();
    public MethodJavaDocComment(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
        if(methodDeclaration.getJavaDoc() != null){
            content = methodDeclaration.getJavaDoc().getContent();
        }
        imports = getImports(methodDeclaration.getParentNode());
        parseContent(content);
    }

    private NodeList<ImportDeclaration> getImports(Optional<Node> parentNode) {
        if(!parentNode.isPresent()){
            return null;
        }

        if(parentNode.get() instanceof CompilationUnit){
            CompilationUnit unit = (CompilationUnit) parentNode.get();
           return unit.getImports();
        }
        return getImports(parentNode.get().getParentNode());
    }

    private void parseContent(String content) {
        if(content == null || "".equals(content)){
            //没有注释
            return;
        }

        content = content.replaceAll("\n\\*","");
        content = content.replaceAll("\\*","");
        content = content.replaceAll("/","");
        System.out.println(content);

        if(content.contains(TAG_RERURN)){
            returnComment = content.substring(content.indexOf(TAG_RERURN)+TAG_RERURN.length());
        }
        System.out.println("returnComment 注释-->: "+returnComment);

        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        if(parameters==null || parameters.isEmpty()){
            //没有参数
            mainContent = content;
            return;
        }

        for (Parameter parameter : parameters) {
            SimpleName name = parameter.getName();
            String identifier = name.getIdentifier();
            identifier = TAG_PARAM+identifier;
            String commentp = "";
            if(content.contains(identifier)){
                 commentp = content.substring(content.indexOf(identifier)+identifier.length());
                if(commentp.contains(TAG_PARAM)){
                    commentp = commentp.substring(0,commentp.indexOf(TAG_PARAM));
                }else if(commentp.contains(TAG_RERURN)){
                    commentp = commentp.substring(0,commentp.indexOf(TAG_RERURN));
                }
                commentp = commentp.trim();
                if(commentp.endsWith("\n")){
                    commentp = commentp.substring(0,commentp.length()-1);
                }
                name.setComment(new JavadocComment(commentp));

                //取出@value作为默认值,存在data中,后续调用demo可使用
                if(commentp.contains(TAG_VALUE)){
                    String value = commentp.substring(commentp.indexOf(TAG_VALUE)+TAG_VALUE.length()).trim();
                    name.setData(KEY_PARAM_VALUE,value);
                    //解析成对应的类型:
                    // Type只能拿到simplename ,拿不到全限定名(包名), 要从import里匹配
                    Type type = parameter.getType();
                    //java.lang.reflect.Type refectType
                    if(type instanceof PrimitiveType){
                        PrimitiveType primitiveType = (PrimitiveType) type;
                        //System.out.println(primitiveType.asString());
                        name.setData(KEY_PARAM_CLASSNAME,primitiveType.asString());
                    }else if(type instanceof ClassOrInterfaceType){
                        ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
                        String typeName = classOrInterfaceType.getName().getIdentifier();
                        if(imports != null){
                            for (ImportDeclaration anImport : imports) {
                                String fullName = anImport.getName().asString().trim();
                                if(fullName.endsWith(typeName)){
                                    typeName = fullName;
                                    break;
                                }
                            }
                        }
                        name.setData(KEY_PARAM_CLASSNAME,typeName);
                        //至此,typeName是全限定名
                        //System.out.println(typeName);
                        //System.out.println(classOrInterfaceType.getName());


                    }



                   /* Gson gson = new Gson();
                    gson.fromJson(value,type);*/

                    /*name.setData(new DataKey<Type>() {
                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public boolean equals(Object obj) {
                            return super.equals(obj);
                        }
                    }, type);*/
                }

            }
            System.out.println(name.getIdentifier()+"  注释-->: "+commentp);

           // System.out.println("name.getComment():"+name.getComment().getContent());
            paramsComment.put(name.getIdentifier(),commentp);

            //name.setData();
            //name.setData(name.getIdentifier(),"");
            //name.setComment(new JavadocComment());
        }
    }

    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }

    private MethodDeclaration methodDeclaration;
}

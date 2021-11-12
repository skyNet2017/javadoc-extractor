package me.tomassetti.javadocextractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import me.tomassetti.javadocextractor.support.DirExplorer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Iterate over all the Javadoc comments and print them together with a description of the commented element.
 */
public class AllJavadocExtractor {

    public static void main(String[] args) {
        File projectDir = new File("source_to_parse2/");

        new DirExplorer(new DirExplorer.Filter() {
            @Override
            public boolean interested(int level, String path, File file) {
                return path.endsWith(".java");
            }
        }, new DirExplorer.FileHandler() {
            @Override
            public void handle(int level, String path, File file) {
                try {
                    new VoidVisitorAdapter<Object>() {
                        @Override
                        public void visit(JavadocComment comment, Object arg) {
                            super.visit(comment, arg);
                            String title = null;
                            if (comment.getCommentedNode().isPresent()) {
                                title = String.format("%s (%s)", describe(comment.getCommentedNode().get()), path);
                            } else {
                                title = String.format("No element associated (%s)", path);
                            }
                            System.out.println(title);
                            System.out.println(Strings.repeat("=", title.length()));
                            System.out.println(comment);

                            System.out.println("--------->");
                            printMethod(comment.getCommentedNode().get());
                        }
                    }.visit(JavaParser.parse(file), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).explore(projectDir);



    }

    private static void printMethod(Node node) {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration)node;
            //JavadocComment javaDoc = method.getJavaDoc();
            MethodJavaDocComment methodJavaDocComment = new MethodJavaDocComment(method);
            //javaDoc.getContent();


           // System.out.println(javaDoc.toString());
           // System.out.println(Arrays.toString(javaDoc.getComment().toArray()));
        }
    }

    private static String describe(Node node) {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration)node;
            return "Method " + methodDeclaration.getDeclarationAsString();
            //public boolean toOrder(long orderId) (/com/hss/demo/JavaDocDemo.java)
        }
        if (node instanceof ConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration)node;
            return "Constructor " + constructorDeclaration.getDeclarationAsString();
        }
        if (node instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration)node;
            if (classOrInterfaceDeclaration.isInterface()) {
                return "Interface " + classOrInterfaceDeclaration.getName();
            } else {
                return "Class " + classOrInterfaceDeclaration.getName();
            }
        }
        if (node instanceof EnumDeclaration) {
            EnumDeclaration enumDeclaration = (EnumDeclaration)node;
            return "Enum " + enumDeclaration.getName();
        }
        if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration)node;
            List<String> varNames = fieldDeclaration.getVariables().stream().map(v -> v.getName().getId()).collect(Collectors.toList());
            return "Field " + String.join(", ", varNames);
        }
        return node.toString();
    }

}

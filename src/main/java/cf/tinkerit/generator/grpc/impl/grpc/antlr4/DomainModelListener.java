package cf.tinkerit.generator.grpc.impl.grpc.antlr4;

import cf.tinkerit.generator.grpc.impl.grpc.model.Field;
import cf.tinkerit.generator.grpc.impl.grpc.model.Type;
import cf.tinkerit.generator.antlr.JavaLexer;
import cf.tinkerit.generator.antlr.JavaParser;
import cf.tinkerit.generator.grpc.impl.utils.CommonUtil;
import cf.tinkerit.generator.grpc.AppDefinedTypeRegistry;
import cf.tinkerit.generator.grpc.impl.JavaInterfaceListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class DomainModelListener extends JavaInterfaceListener {
    private Type type;
    private BufferedTokenStream tokens;

    public DomainModelListener(AppDefinedTypeRegistry registry, BufferedTokenStream tokens) {
        super(registry, tokens);
        this.tokens = tokens;
        this.type = new Type();
        this.type.setComplexType(true);
        this.type.setFields(new ArrayList<>(50));
    }

    public Type getType() {
        return type;
    }


    @Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        this.type.setName(ctx.IDENTIFIER().getText());
        if (ctx.EXTENDS() != null) {
            Type parent = resolveType(ctx.typeType().classOrInterfaceType().IDENTIFIER(0).getText());
            this.type.setParent(parent);
        }
    }

    @Override
    public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        if (ctx.getParent() instanceof JavaParser.TypeDeclarationContext) {
            this.type.setName(ctx.IDENTIFIER().getText());
        }
    }

    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        // ignore static fields
        if (ctx.parent.parent instanceof JavaParser.ClassBodyDeclarationContext) {
            JavaParser.ClassBodyDeclarationContext cbdc
                = (JavaParser.ClassBodyDeclarationContext) ctx.parent.parent;
            boolean isStatic = cbdc.modifier()
                .stream()
                .filter(m -> m.classOrInterfaceModifier() != null)
                .map(JavaParser.ModifierContext::classOrInterfaceModifier)
                .anyMatch(m -> m.STATIC() != null);
            if (isStatic) {
                return;
            }

            Field field = new Field();
            // assume one field per one declaration
            field.setName(ctx.variableDeclarators().variableDeclarator().get(0).variableDeclaratorId().IDENTIFIER().getText());
            field.setType(ctx.typeType().getText());

            // get innermost type
            Type fieldType = resolveType(field.getType());
            field.setRichType(fieldType);

            // get field description from comment
            List<Token> cmtTokens = tokens.getHiddenTokensToLeft(cbdc.start.getTokenIndex(), JavaLexer.COMMENTS);
            if (cmtTokens != null && !cmtTokens.isEmpty()) {
                StringBuilder buf = new StringBuilder();
                for (Token cmtToken: cmtTokens) {
                    String cmt = cmtToken.getText();
                    if (cmt != null) {
                        String[] lines = cmt.split(System.lineSeparator());
                        // use first line as field description
                        if (lines.length > 0) {
                            for (String line: lines) {
                                buf.append(CommonUtil.stripCommentMarker(line));
                                buf.append(" ");
                            }
                        }
                    }
                }
                field.setDescription(buf.toString().trim());
            }
            this.type.getFields().add(field);
        }

    }
}

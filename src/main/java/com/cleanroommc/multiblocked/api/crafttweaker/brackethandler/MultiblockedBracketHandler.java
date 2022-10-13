package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import crafttweaker.zenscript.IBracketHandler;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.List;

/**
 * @author youyihj
 */
public abstract class MultiblockedBracketHandler implements IBracketHandler {
    protected final IJavaMethod method;
    protected final String key;
    protected final Class<?> returnedClass;

    protected MultiblockedBracketHandler(IJavaMethod method, String key, Class<?> returnedClass) {
        this.method = method;
        this.key = key;
        this.returnedClass = returnedClass;
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if ((tokens.size() < 5)) return null;
        if (!tokens.get(0).getValue().equalsIgnoreCase("mbd")) return null;
        if (!tokens.get(1).getValue().equals(":")) return null;
        if (!tokens.get(2).getValue().equals(key)) return null;
        if (!tokens.get(3).getValue().equals(":")) return null;
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 4; i < tokens.size(); i++) {
            nameBuilder.append(tokens.get(i).getValue());
        }
        return position -> new ExpressionCallStatic(position, environment, method, new ExpressionString(position, nameBuilder.toString()));
    }

    @Override
    public Class<?> getReturnedClass() {
        return returnedClass;
    }

    @Override
    public String getRegexMatchingString() {
        return "mbd:" + key + ":.*";
    }
}

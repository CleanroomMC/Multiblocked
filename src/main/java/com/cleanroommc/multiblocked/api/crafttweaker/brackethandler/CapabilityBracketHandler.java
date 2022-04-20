package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.crafttweaker.CTRegistry;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.zenscript.IBracketHandler;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.List;

@BracketHandler
@ZenRegister
public class CapabilityBracketHandler implements IBracketHandler {
    private final IJavaMethod method;

    public CapabilityBracketHandler() {
        this.method = CraftTweakerAPI.getJavaMethod(CapabilityBracketHandler.class, "get", String.class);
    }

    public static MultiblockCapability<?> get(String member) {
        return CTRegistry.getCapability(member);
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if ((tokens.size() < 5)) return null;
        if (!tokens.get(0).getValue().equalsIgnoreCase("mbd")) return null;
        if (!tokens.get(1).getValue().equals(":")) return null;
        if (!tokens.get(2).getValue().equals("cap")) return null;
        if (!tokens.get(3).getValue().equals(":")) return null;
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 4; i < tokens.size(); i++) {
            nameBuilder.append(tokens.get(i).getValue());
        }
        return position -> new ExpressionCallStatic(position, environment, method, new ExpressionString(position, nameBuilder.toString()));
    }
}

package com.cleanroommc.multiblocked.api.crafttweaker.brackethandler;

import com.cleanroommc.multiblocked.api.crafttweaker.CTRegistry;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
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
public class RecipeMapBracketHandler implements IBracketHandler {
    private final IJavaMethod method;

    public RecipeMapBracketHandler() {
        this.method = CraftTweakerAPI.getJavaMethod(RecipeMapBracketHandler.class, "get", String.class);
    }

    public static RecipeMap get(String member) {
        return CTRegistry.getRecipeMap(member);
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if ((tokens.size() < 5)) return null;
        if (!tokens.get(0).getValue().equalsIgnoreCase("mbd")) return null;
        if (!tokens.get(1).getValue().equals(":")) return null;
        if (!tokens.get(2).getValue().equals("recipe_map")) return null;
        if (!tokens.get(3).getValue().equals(":")) return null;
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 4; i < tokens.size(); i++) {
            nameBuilder.append(tokens.get(i).getValue());
        }
        return position -> new ExpressionCallStatic(position, environment, method, new ExpressionString(position, nameBuilder.toString()));
    }
}

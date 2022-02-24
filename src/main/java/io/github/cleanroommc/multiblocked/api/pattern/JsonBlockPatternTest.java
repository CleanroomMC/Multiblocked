package io.github.cleanroommc.multiblocked.api.pattern;

import io.github.cleanroommc.multiblocked.api.pattern.util.RelativeDirection;

class JsonBlockPatternTest {
    public static void main(String[] args) {
        JsonBlockPattern pattern = new JsonBlockPattern();
        pattern.pattern = new String[][] {
                {"TXX", " E "},
                {"C#A", "QPW"},
                {"BYD", "   "}
        };
        print(pattern);

        pattern.changeDir(RelativeDirection.DOWN, RelativeDirection.FRONT, RelativeDirection.RIGHT);
        print(pattern);

        pattern.changeDir(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT);
        print(pattern);

    }

    private static void print(JsonBlockPattern pattern) {
        for (int i = 0; i < pattern.pattern.length; i++) {
            for (int j = 0; j < pattern.pattern[0].length; j++) {
                System.out.print("\""+pattern.pattern[i][j] + "\"  ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
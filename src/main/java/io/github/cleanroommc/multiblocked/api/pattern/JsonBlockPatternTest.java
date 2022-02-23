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
        for (int i = 0; i < pattern.pattern.length; i++) {
            for (int j = 0; j < pattern.pattern[0].length; j++) {
                System.out.print("\""+pattern.pattern[i][j] + "\"  ");
            }
            System.out.println();
        }
        System.out.println();

        pattern.changeDir(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT);
        for (int i = 0; i < pattern.pattern.length; i++) {
            for (int j = 0; j < pattern.pattern[0].length; j++) {
                System.out.print("\""+pattern.pattern[i][j] + "\"  ");
            }
            System.out.println();
        }
        System.out.println();

        pattern.pattern = new String[][] {
                {"TXX", " E "},
                {"C#A", "QPW"},
                {"BYD", "   "}
        };
        pattern.changeDir(RelativeDirection.DOWN, RelativeDirection.FRONT, RelativeDirection.RIGHT);
        for (int i = 0; i < pattern.pattern.length; i++) {
            for (int j = 0; j < pattern.pattern[0].length; j++) {
                System.out.print("\""+pattern.pattern[i][j] + "\"  ");
            }
            System.out.println();
        }
        System.out.println();

    }
}
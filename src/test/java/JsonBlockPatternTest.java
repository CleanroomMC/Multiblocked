import io.github.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.util.RelativeDirection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

class JsonBlockPatternTest {
    public static void main(String[] args) {
        JsonBlockPattern pattern = new JsonBlockPattern();
        pattern.pattern = new String[][] {
                {"TXX", " E "},
                {"C#A", "QPW"},
                {"BYD", "   "}
        };
        print(pattern);

        BlockPos pos = pattern.getActualPosOffset(2, 0,1, EnumFacing.NORTH);
        System.out.println(pos);
        System.out.println();


        pattern.changeDir(RelativeDirection.DOWN, RelativeDirection.BACK, RelativeDirection.RIGHT);
        print(pattern);

        System.out.println(Arrays.toString(pattern.getActualPatternOffset(pos, EnumFacing.NORTH)));
        System.out.println();

        pattern.changeDir(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT);
        print(pattern);

        System.out.println(Arrays.toString(pattern.getActualPatternOffset(pos, EnumFacing.NORTH)));
        System.out.println();

    }

    private static void print(JsonBlockPattern pattern) {
        for (int i = 0; i < pattern.pattern.length; i++) {
            for (int j = 0; j < pattern.pattern[0].length; j++) {
                System.out.print("\""+pattern.pattern[i][j] + "\"  ");
            }
            System.out.println();
        }
    }
}
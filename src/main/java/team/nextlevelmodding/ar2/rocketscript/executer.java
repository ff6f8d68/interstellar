package team.nextlevelmodding.ar2.rocketscript;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;
import net.minecraft.core.BlockPos;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class executer {

    private static final Map<BlockPos, Globals> vmMap = new ConcurrentHashMap<>();

    public static void runScript(InputStream scriptStream, BlockPos pos) {
        try {
            // Read InputStream into byte array
            byte[] scriptBytes = scriptStream.readAllBytes();

            // Get or create VM for this BlockPos
            Globals globals = vmMap.computeIfAbsent(pos, k -> {
                Globals g = JsePlatform.standardGlobals();
                g.set("pos", CoerceJavaToLua.coerce(k));
                return g;
            });

            // Load Lua bytecode (.rsc) from byte array
            Prototype p = globals.loadPrototype(new ByteArrayInputStream(scriptBytes), "script", "b");
            LuaClosure chunk = new LuaClosure(p, globals);

            // Execute the script
            chunk.call();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeVM(BlockPos pos) {
        vmMap.remove(pos);
    }
}

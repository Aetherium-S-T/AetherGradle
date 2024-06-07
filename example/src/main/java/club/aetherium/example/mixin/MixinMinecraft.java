package club.aetherium.example.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "createDisplay", at = @At("RETURN"))
    public void inject$createDisplay(CallbackInfo callbackInfo) {
        Display.setTitle("Example Client | 1.8.9");
    }

    @Inject(method = "startGame", at = @At("RETURN"))
    public void inject$startGame(CallbackInfo callbackInfo) {
        System.out.println("Hello, Mixin!");
    }
}

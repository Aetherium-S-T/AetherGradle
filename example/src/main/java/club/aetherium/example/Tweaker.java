package club.aetherium.example;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Tweaker implements ITweaker {
    private final List<String> launchArguments = new ArrayList<>();

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.launchArguments.addAll(args);

        if (!args.contains("--version") && profile != null) {
            launchArguments.add("--version");
            launchArguments.add(profile);
        }

        if (!args.contains("--assetsDir") && assetsDir != null) {
            launchArguments.add("--assetsDir");
            launchArguments.add(assetsDir.getAbsolutePath());
        }

        if (!args.contains("--gameDir") && gameDir != null) {
            launchArguments.add("--gameDir");
            launchArguments.add(gameDir.getAbsolutePath());
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        MixinBootstrap.init();

        MixinEnvironment environment = MixinEnvironment.getDefaultEnvironment();

        environment.addConfiguration("example.mixins.json");

        if (environment.getObfuscationContext() == null) {
            environment.setObfuscationContext("notch");
        }

        environment.setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return launchArguments.toArray(new String[0]);
    }
}

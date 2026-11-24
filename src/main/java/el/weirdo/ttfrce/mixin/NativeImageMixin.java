package el.weirdo.ttfrce.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.util.freetype.FreeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(NativeImage.class)
public class NativeImageMixin {
    @ModifyArg(
            method = "copyFromFont",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/util/freetype/FreeType;FT_Load_Glyph(Lorg/lwjgl/util/freetype/FT_Face;II)I",
                    remap = false
            ),
            index = 2
    )
    private int copyFromFont(int loadFlags) {
        return (loadFlags & ~FreeType.FT_LOAD_FORCE_AUTOHINT) | FreeType.FT_LOAD_NO_AUTOHINT;
    }
}

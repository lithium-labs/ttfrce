package el.weirdo.ttfrce.mixin;

import el.weirdo.ttfrce.SfntValidator;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.freetype.FreeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(TrueTypeGlyphProviderDefinition.class)
public class TrueTypeGlyphProviderDefinitionMixin {
    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/util/freetype/FreeType;FT_New_Memory_Face(JLjava/nio/ByteBuffer;JLorg/lwjgl/PointerBuffer;)I",
                    remap = false
            )
    )
    private int load(long library, ByteBuffer file_base, long face_index, PointerBuffer aface) {
        if (!SfntValidator.isSafe(file_base)) {
            return 2; // invalid file format error
        }
        return FreeType.FT_New_Memory_Face(library, file_base, face_index, aface);
    }
}

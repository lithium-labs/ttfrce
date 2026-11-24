package el.weirdo.ttfrce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SfntValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger("ttfrce");

    private static final int MAX_ALLOWED_STACK_ELEMENTS = 2048;
    private static final int MAX_ALLOWED_POINTS = 16384;
    private static final int MAX_ALLOWED_CONTOURS = 4096;

    private SfntValidator() {}

    public static boolean isSafe(ByteBuffer rawBuffer) {
        if (rawBuffer == null || rawBuffer.remaining() < 12) {
            return false;
        }

        ByteBuffer buf = rawBuffer.duplicate().order(ByteOrder.BIG_ENDIAN);
        int size = buf.remaining();

        int sfntVersion = buf.getInt(0);

        //                 classic                      OTTO                         true                         typ1
        if (sfntVersion != 0x00010000 && sfntVersion != 0x4F54544F && sfntVersion != 0x74727565 && sfntVersion != 0x74797031) {
            LOGGER.warn("Blocked font with unknown SFNT version");
            return false;
        }

        int numTables = Short.toUnsignedInt(buf.getShort(4));
        if (numTables == 0 || numTables > 64 || 12 + numTables * 16 > size) {
            LOGGER.warn("Blocked font with invalid table count");
            return false;
        }

        int maxpOffset = -1;
        int maxpLength = 0;

        for (int i = 0; i < numTables; i++) {
            int recordPos = 12 + i * 16;
            int tag = buf.getInt(recordPos);
            int offset = buf.getInt(recordPos + 8);
            int length = buf.getInt(recordPos + 12);

            if (offset < 0 || length < 0 || (long) offset + (long) length > size) {
                LOGGER.warn("Blocked font with out of bounds SFNT table");
                return false;
            }

            // maxp
            if (tag == 0x6D617870) {
                maxpOffset = offset;
                maxpLength = length;
            }
        }

        if (maxpOffset != -1 && maxpLength >= 32) {
            int maxpVersion = buf.getInt(maxpOffset);
            if (maxpVersion == 0x00010000) {
                int maxPoints = Short.toUnsignedInt(buf.getShort(maxpOffset + 6));
                int maxContours = Short.toUnsignedInt(buf.getShort(maxpOffset + 8));
                int maxCompositePoints = Short.toUnsignedInt(buf.getShort(maxpOffset + 10));
                int maxCompositeContours = Short.toUnsignedInt(buf.getShort(maxpOffset + 12));
                int maxStackElements = Short.toUnsignedInt(buf.getShort(maxpOffset + 24));

                if (maxStackElements > MAX_ALLOWED_STACK_ELEMENTS) {
                    LOGGER.warn("Blocked font whose maxStackElements exceeded {}", MAX_ALLOWED_STACK_ELEMENTS);
                    return false;
                }
                if (maxPoints > MAX_ALLOWED_POINTS || maxCompositePoints > MAX_ALLOWED_POINTS
                        || maxContours > MAX_ALLOWED_CONTOURS || maxCompositeContours > MAX_ALLOWED_CONTOURS) {
                    LOGGER.warn("Blocked font with excessive point or contour declarations");
                    return false;
                }
            }
        }

        return true;
    }
}
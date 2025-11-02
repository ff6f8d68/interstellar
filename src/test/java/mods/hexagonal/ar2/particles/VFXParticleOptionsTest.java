package mods.hexagonal.ar2.particles;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VFXParticleOptions.DESERIALIZER.fromNetwork method
 */
class VFXParticleOptionsTest {

    private VFXParticleOptions.Deserializer<VFXParticleOptions> deserializer;

    @Mock
    private ParticleType<VFXParticleOptions> particleType;

    @Mock
    private FriendlyByteBuf buf;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deserializer = (VFXParticleOptions.Deserializer<VFXParticleOptions>) VFXParticleOptions.DESERIALIZER;
    }

    @Test
    void testFromNetworkValidRGBValues() {
        // Arrange
        float r = 0.5f;
        float g = 0.75f;
        float b = 0.25f;

        when(buf.readFloat()).thenReturn(r).thenReturn(g).thenReturn(b);

        // Act
        VFXParticleOptions result = deserializer.fromNetwork(particleType, buf);

        // Assert
        assertNotNull(result);
        assertEquals(r, result.r());
        assertEquals(g, result.g());
        assertEquals(b, result.b());
        verify(buf, times(3)).readFloat();
    }

    @Test
    void testFromNetworkZeroRGBValues() {
        // Arrange
        float r = 0.0f;
        float g = 0.0f;
        float b = 0.0f;

        when(buf.readFloat()).thenReturn(r).thenReturn(g).thenReturn(b);

        // Act
        VFXParticleOptions result = deserializer.fromNetwork(particleType, buf);

        // Assert
        assertNotNull(result);
        assertEquals(0.0f, result.r());
        assertEquals(0.0f, result.g());
        assertEquals(0.0f, result.b());
    }

    @Test
    void testFromNetworkMaximumRGBValues() {
        // Arrange
        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;

        when(buf.readFloat()).thenReturn(r).thenReturn(g).thenReturn(b);

        // Act
        VFXParticleOptions result = deserializer.fromNetwork(particleType, buf);

        // Assert
        assertNotNull(result);
        assertEquals(1.0f, result.r());
        assertEquals(1.0f, result.g());
        assertEquals(1.0f, result.b());
    }

    @Test
    void testFromNetworkNegativeRGBValues() {
        // Arrange - Testing with negative values (which are technically valid for float, but unusual for RGB)
        float r = -0.5f;
        float g = -0.25f;
        float b = -0.75f;

        when(buf.readFloat()).thenReturn(r).thenReturn(g).thenReturn(b);

        // Act
        VFXParticleOptions result = deserializer.fromNetwork(particleType, buf);

        // Assert
        assertNotNull(result);
        assertEquals(-0.5f, result.r());
        assertEquals(-0.25f, result.g());
        assertEquals(-0.75f, result.b());
    }

    @Test
    void testFromNetworkNullBufferThrowsException() {
        // Arrange - Pass null buffer

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            deserializer.fromNetwork(particleType, null);
        });
    }
}
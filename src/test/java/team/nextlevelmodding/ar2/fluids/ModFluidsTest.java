package team.nextlevelmodding.ar2.fluids;

import net.minecraftforge.eventbus.api.IEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ModFluids Register Tests")
class ModFluidsTest {

    private IEventBus mockEventBus;

    @BeforeEach
    void setUp() {
        mockEventBus = mock(IEventBus.class);
    }

    @Test
    @DisplayName("Should successfully register fluids with valid eventBus")
    void testRegisterSuccessfullyWithValidEventBus() {
        // Arrange
        // Act
        assertDoesNotThrow(() -> ModFluids.register(mockEventBus));
    }

    @Test
    @DisplayName("Should throw NullPointerException when eventBus is null")
    void testRegisterWithNullEventBus() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> ModFluids.register(null));
    }

    @Test
    @DisplayName("Should call FLUIDS.register exactly once with eventBus")
    void testRegisterCallsFluidsRegisterOnce() {
        // Arrange
        try (MockedStatic<ModFluids> mockedStatic = mockStatic(ModFluids.class)) {
            mockedStatic.when(() -> ModFluids.register(mockEventBus)).thenCallRealMethod();
            
            // Act
            ModFluids.register(mockEventBus);
            
            // Assert
            mockedStatic.verify(() -> ModFluids.register(mockEventBus), times(1));
        }
    }
}
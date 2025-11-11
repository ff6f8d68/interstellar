package team.nextlevelmodding.ar2.ships;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public enum ForceDirectionMode implements StringRepresentable {
	SHIP("ship"), WORLD("world");

	private final String name;

	ForceDirectionMode(String name) {
		this.name = name;
	}

	@Override
	public @NotNull String getSerializedName() {
		return this.name;
	}
}

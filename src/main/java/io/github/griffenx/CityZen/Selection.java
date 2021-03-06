package io.github.griffenx.CityZen;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;

public class Selection {
	public Position pos1;
	public Position pos2;

	public Selection(Position pos1, Position pos2) {
		if (!pos1.world.equals(pos2.world))
			return;
		this.pos1 = pos1;
		this.pos2 = pos2;
	}

	public Double getArea() {
		return Math.abs((pos1.x - pos2.x) * (pos1.z - pos2.z));
	}

	public Double getSideX() {
		return Math.abs(pos1.x - pos2.x);
	}

	public Double getSideZ() {
		return Math.abs(pos1.z - pos2.z);
	}

	public Selection getBuffer(double bufferAdjustment) {
		final double bufferSize = CityZen.getPlugin().getConfig().getDouble("plotBuffer") + bufferAdjustment;
		if (bufferSize <= 0)
			return this;
		final Position bpos1 = new Position(pos1.world, pos1.x + (pos1.x < pos2.x ? -1 : 1) * bufferSize, pos1.y,
				pos1.z + (pos1.z < pos2.z ? -1 : 1) * bufferSize);
		final Position bpos2 = new Position(pos2.world, pos2.x + (pos1.x > pos2.x ? -1 : 1) * bufferSize, pos2.y,
				pos2.z + (pos1.z > pos2.z ? -1 : 1) * bufferSize);
		return new Selection(bpos1, bpos2);
	}

	public Selection getBuffer() {
		return getBuffer(0);
	}

	public boolean isSpaced(City affiliation) {
		double xLow = pos1.x;
		double xHigh = pos2.x;
		double zLow = pos1.z;
		double zHigh = pos2.z;
		if (pos2.x < xLow) {
			xLow = pos2.x;
			xHigh = pos1.x;
		}
		if (pos2.z < zLow) {
			zLow = pos2.z;
			zHigh = pos1.z;
		}

		for (double x = xLow; x <= xHigh; x++)
			for (double z = zLow; z <= zHigh; z++)
				for (final City c : City.getCities())
					if (!c.equals(affiliation) && c.getCenter() != null)
						if (c.isInCity(x, z) || (c.getWorld().equals(pos1.world)
								&& Util.getDistace(new Position(pos1.world, x, 0, z), c.getCenter()) < CityZen
										.getPlugin().getConfig().getInt("minCitySeparation")))
							return false;
		return true;
	}

	public boolean worldGuardConflicts(Citizen citizen) {
		if (CityZen.WorldGuard != null) {
			double xLow = pos1.x;
			double xHigh = pos2.x;
			double zLow = pos1.z;
			double zHigh = pos2.z;
			if (pos2.x < xLow) {
				xLow = pos2.x;
				xHigh = pos1.x;
			}
			if (pos2.z < zLow) {
				zLow = pos2.z;
				zHigh = pos1.z;
			}

			for (double y = 1; y <= pos1.world.getMaxHeight(); y++)
				for (double x = xLow; x <= xHigh; x++)
					for (double z = zLow; z <= zHigh; z++)
						if (!WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testState(
								BukkitAdapter.adapt(new Location(pos1.world, x, y, z)),
								WorldGuardPlugin.inst().wrapPlayer(citizen.getPlayer()), Flags.BUILD))
							return true;
		}
		return false;
	}
}

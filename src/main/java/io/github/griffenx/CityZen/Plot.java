package io.github.griffenx.CityZen;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class Plot {
	private int identifier;
	private City affiliation;
	
	private static final CityLog log = CityZen.cityLog;
	
	/**
	 * Initializes a Plot so that it can be loaded from config
	 * @param city
	 * The City in which this Plot resides
	 * @param id
	 * The ID number of this Plot
	 */
	private Plot(City city, Location corner1, Location corner2, Citizen creator) {
		identifier = generateID(city);
		affiliation = city;
		ConfigurationSection properties = CityZen.cityConfig.getConfig().getConfigurationSection("cities." + city.getIdentifier() + ".plots")
				.createSection(Integer.toString(identifier));
		properties.set("corner1", corner1.getBlockX() + "," + corner1.getBlockZ());
		properties.set("corner2", corner2.getBlockX() + "," + corner2.getBlockZ());
		properties.set("creator", creator.getUUID().toString());
		properties.set("height", (int)((corner1.getBlockY() + corner2.getBlockY())/2));
		properties.set("price", 0);
		properties.set("mega", false);
		properties.set("protection", 2);
		properties.set("owners", new Vector<String>());
		CityZen.cityConfig.save();
	}
	
	private Plot(City city, int id) throws IllegalArgumentException {
		if (CityZen.cityConfig.getConfig().getConfigurationSection("cities." + city.getIdentifier() + ".plots." + id)
				== null) throw new IllegalArgumentException("Attempted to get a Plot that doesn't exist");
		else {
			identifier = id;
			affiliation = city;
		}
	}
	
	/**
	 * Create a brand-spanking new Plot from scratch
	 * @param city
	 * The City in which this Plot resides
	 * @param corner1
	 * The Location of this Plot's first corner
	 * @param corner2
	 * The Location of this Plot's second corner
	 * @param creator
	 * The Citizen who created this Plot
	 * @return
	 * The freshly-squeezed new Plot. If this plot already exists, or would overlap an existing plot, this returns {@literal null}.
	 */
	public static Plot createPlot(City city, Location corner1, Location corner2, Citizen creator) {
		for (Plot p : city.getPlots()) if (p.overlaps(corner1, corner2)) return null;
		Plot newPlot = new Plot(city, corner1, corner2, creator);
		return newPlot;
	}
	
	/**
	 * Creates a new plot, but does not add the creator as an owner
	 * @param city
	 * The City in which to create this Plot
	 * @param corner1
	 * The first corner of this Plot
	 * @param corner2
	 * The second corner of this Plot
	 * @param creator
	 * The Citizen who created this Plot
	 * @return
	 * A new Plot with no owners
	 */
	public static Plot createEmptyPlot(City city, Location corner1, Location corner2, Citizen creator) {
		Plot newPlot = createPlot(city, corner1, corner2, creator);
		//newPlot.addOwner(creator);
		return newPlot;
	}
	
	/**
	 * Gets the plot that includes the specified location. If not plot is found at the location, returns null.
	 * @param location
	 * The Location to check for existing plots
	 * @return
	 * The Plot that contains that Location, or null if no Plot is found
	 */
	public static Plot getPlot(Location location) {
		for (City c : City.getCities()) {
			log.debug("Checking for overlapped plot in City " + c.getName());
			log.debug("City identifier: " + c.getIdentifier());
			
			Plot p = c.getPlot(location);
			if (p != null) {
				log.debug("Found plot at location.");
				return p;
			} else log.debug("Couldn't find a plot in this city at this location.");
		}
		log.write("Couldn't find a plot at this location. Returning Null");
		return null;
	}
	
	/**
	 * Gets a specific plot from a City.
	 * @param city
	 * The City to which this plot belongs
	 * @param identifier
	 * The ID number of the plot to get
	 * @return
	 * A Plot from the specified City that has the specified identifier 
	 */
	public static Plot getPlot(City city, int identifier) {
		for (String key : CityZen.cityConfig.getConfig().getConfigurationSection("cities." + city.getIdentifier() + ".plots").getKeys(false)) {
			if (Integer.parseInt(key) == identifier) return new Plot(city,identifier);
		} return null;
	}
	
	/**
	 * Deletes this plot from its City's config
	 */
	public void delete() {
		CityZen.cityConfig.getConfig().set("cities." + getAffiliation().getIdentifier() + ".plots." + getIdentifier(), null);
	}
	
	/**
	 * Gets this Plot's ID number, making this a read-only property
	 * @return
	 * This Plot's ID number
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Gets the first corner of this Plot
	 * @return
	 * The Location representing the first corner of this plot
	 */
	public Location getCorner1() {
		String coords = getProperty("corner1");
		return (new Location(getAffiliation().getWorld(), Double.valueOf(coords.split(",")[0]), 0, Double.valueOf(coords.split(",")[1])));
	}
	
	/**
	 * Sets the first corner of this Plot
	 * @param location
	 * The Location to set to this Plot's first corner
	 */
	public void setCorner1(Location location) {
		String coords = ((int) location.getX()) + "," + ((int) location.getZ());
		setProperty("corner1",coords);
	}
	
	/**
	 * Gets the second corner of this Plot
	 * @return
	 * The Location representing the second corner of this plot
	 */
	public Location getCorner2() {
		String coords = getProperty("corner2");
		return (new Location(getAffiliation().getWorld(), Double.valueOf(coords.split(",")[0]), 0, Double.valueOf(coords.split(",")[1])));
	}
	
	/**
	 * Sets the second corner of this Plot
	 * @param location
	 * The Location to set to this Plot's second corner
	 */
	public void setCorner2(Location location) {
		String coords = ((int) location.getX()) + "," + ((int) location.getZ());
		setProperty("corner2",coords);
	}
	
	/**
	 * Gets the location representing the center of this plot.
	 * @return
	 * A Location object representing this Plot's geographic center in the XZ plane.
	 */
	public Location getCenter() {
		Selection sel = getSelection();
		return new Location(getCorner1().getWorld(), (sel.pos1.x + sel.pos2.x) / 2, getBaseHeight(), (sel.pos1.z + sel.pos2.z) / 2);
	}
	
	/**
	 * Shortcut to get the string representation of this Plot's center.
	 * @return
	 * This plot's X and Z coords as a string, as Cartesian coordinates.
	 */
	public String getCenterCoords() {
		return "(" + getCenter().getBlockX() + "," + getCenter().getBlockZ() + ")";
	}
	
	public Selection getSelection() {
		return new Selection(new Position(getCorner1()), new Position(getCorner2()));
	}
	
	/**
	 * Gets whether or not this is a MegaPlot
	 * @return
	 * True if this is a MegaPlot
	 */
	public Boolean isMega() {
		return Boolean.valueOf(getProperty("mega"));
	}
	
	/**
	 * Sets whether or not this is a MegaPlot
	 * @param state
	 * Whether or not this is a MegaPlot
	 */
	public void setMega(Boolean state) {
		setProperty("mega",state);
	}
	
	/**
	 * Gets the base height for this plot. Used for wiping to flatlands.
	 * @return
	 * The height of this Plot's baseHeight
	 */
	public Integer getBaseHeight() {
		Integer height = null;
		try {
			height = Integer.valueOf(getProperty("height"));
		} catch (NumberFormatException e){}
		return height;
	}
	
	/**
	 * Sets the base height of this plot
	 * @param height
	 * The height level to set for this Plot's baseHeight
	 */
	public void setBaseHeight(int height) {
		setProperty("height",height);
	}
	
	/**
	 * Gets the protection level for this Plot.
	 * 2 - Protected, only owners can build here
	 * 1 - Communal, only citizens of the city can build here
	 * 0 - Public, anyone can build here
	 * @return
	 * The protection level for this plot.
	 */
	public ProtectionLevel getProtectionLevel() {
		try {
			int levelIndex = Integer.valueOf(getProperty("protection"));
			if (levelIndex > 2 || levelIndex < 0) levelIndex = 2;
			return ProtectionLevel.values()[levelIndex];
		} catch (NumberFormatException e) {
			return ProtectionLevel.PROTECTED;
		}
	}
	
	/**
	 * Sets the protection level for this Plot.
	 * 2 - Protected, only owners can build here
	 * 1 - Communal, only citizens of the city can build here
	 * 0 - Public, anyone can build here
	 * @param level
	 * The protection level to set for this plot
	 */
	public void setProtectionLevel(int level) {
		if (level >=0 && level < 3) setProperty("protection",level);
		else setProperty("protection",2);
	}
	
	/**
	 * Sets the protection level for this Plot
	 * @param level
	 * The exact protection level to set
	 */
	public void setProtectionLevel(ProtectionLevel level) {
		setProperty("protection",ProtectionLevel.getIndex(level));
	}
	
	/**
	 * Gets the City with which this Plot is affiliated. Read-only property.
	 * @return
	 * The City that this Plot is in
	 */
	public City getAffiliation() {
		return affiliation;
	}
	
	/**
	 * Gets the list of owners of this Plot.
	 * @return
	 * List of Citizens that are registered as this Plot's owners.
	 */
	public List<Citizen> getOwners() {
		List<Citizen> owners = new Vector<Citizen>();
		ConfigurationSection section = CityZen.cityConfig.getConfig().getConfigurationSection("cities." + getAffiliation().getIdentifier() + ".plots." + identifier);
		for (String u : section.getStringList("owners")) {
			owners.add(Citizen.getCitizen(UUID.fromString(u)));
		}
		return owners;
	}
	
	/**
	 * Adds an owner to this Plot
	 * @param owner
	 * The Citizen to register as an owner of this Plot
	 */
	public void addOwner(Citizen owner) {
		List<String> owners = new Vector<String>();
		if (getAffiliation().equals(owner.getAffiliation())) {
			for (Citizen u : getOwners()) {
				if (!u.equals(owner)) {
					owners.add(u.getPassport().getUniqueId().toString());
				}
			}
			owners.add(owner.getPassport().getUniqueId().toString());
			setProperty("owners",owners);
		}
	}
	
	/**
	 * Removes a Citizen as an owner of this Plot. If there are no longer any owners of this plot, wipe it.
	 * @param owner
	 * The Citizen to remove as an owner
	 */
	public void removeOwner(Citizen owner) {
		List<String> owners = new Vector<String>();
		for (Citizen u : getOwners()) {
			if (!u.equals(owner)) {
				owners.add(u.getPassport().getUniqueId().toString());
			}
		}
		setProperty("owners",owners);
	}
	
	/**
	 * Get the Citizen who created this Plot. They need not be an owner.
	 * @return
	 * The Citizen who created this Plot. Returns null if the creator is not set properly
	 */
	public Citizen getCreator() {
		try {
			return Citizen.getCitizen(UUID.fromString(getProperty("creator")));
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Sets the creator of this Plot. For adjustment and creation purposes only.
	 * @param creator
	 * The Citizen to set as this Plot's creator.
	 */
	public void setCreator(Citizen creator) {
		setProperty("creator",creator.getPassport().getUniqueId().toString());
	}
	
	/**
	 * Gets the selling price of this Plot. Sets to 0 if this plot is a MegaPlot (cannot be sold)
	 * @return
	 * The price of this plot
	 */
	public double getPrice() {
		double price;
		try {
			price = Double.valueOf(getProperty("price"));
			if (price < 0 || isMega()) price = 0;
		} catch (NumberFormatException e) {
			price = 0;
		}
		return price;
	}
	
	/**
	 * Sets the price of this plot.
	 * @param price
	 * The price to set for this plot. Price cannot be less than 0. Price is set to 0 if this is a MegaPlot.
	 */
	public void setPrice(double price) {
		if (price < 0 || isMega()) price = 0;
		setProperty("price", price);
	}
	
	/**
	 * Calculates the area of this Plot based on the coordinates of its corners.
	 * @return
	 * This Plot's area, in meters squared
	 */
	public Double getArea() {
		return Math.abs((getCorner1().getX() - getCorner2().getX()) * (getCorner1().getZ() - getCorner2().getZ()));
	}
	
	/**
	 * Determines whether or not the specified Location is inside this Plot.
	 * @param location
	 * The Location to check
	 * @return
	 * True if location is inside the plot, else false
	 */
	public boolean isInPlot(Location location) {
		return isInPlot(location.getBlockX(), location.getBlockZ());
	}
	/**
	 * Determines whether or not the specified coordinates are inside this Plot.
	 * @param x
	 * The X Coordinate to check
	 * @param z
	 * The Z Coordinate to check
	 * @return
	 * True if (x,z) is inside this plot, else false
	 */
	public boolean isInPlot(double x, double z) {
		log.debug("Checking coordinates: (" + x + "," + z + ")");
		log.debug("Corner 1: (" + getCorner1().getX() + "," + getCorner1().getZ() + ")");
		log.debug("Corner 2: (" + getCorner2().getX() + "," + getCorner2().getZ() + ")");
		if ((x <= getCorner2().getX() && x >= getCorner1().getX()) || (x >= getCorner2().getX() && x <= getCorner1().getX())) {
			if ((z <= getCorner2().getZ() && z >= getCorner1().getZ()) || (z >= getCorner2().getZ() && z <= getCorner1().getZ())) {
				return true;
			}
		}
		return false;
	}
	public boolean isInPlot(Position p) {
		return isInPlot((double)p.x,(double)p.z);
	}
	
	
	public boolean isInBuffer(double x, double z) {
		if (isInPlot(x,z)) return false;
		double buffer = CityZen.getPlugin().getConfig().getDouble("plotBuffer");
		if (buffer <= 0) return false;
		double[] bufferX = {
			getCorner1().getX() + (getCorner1().getX() < getCorner2().getX() ? -1 : 1) * buffer,
			getCorner2().getX() + (getCorner1().getX() < getCorner2().getX() ? 1 : -1) * buffer
		};
		double[] bufferZ = {
			getCorner1().getZ() + (getCorner1().getZ() < getCorner2().getZ() ? -1 : 1) * buffer,
			getCorner2().getZ() + (getCorner1().getZ() < getCorner2().getZ() ? 1 : -1) * buffer
		};
		if ((x <= bufferX[0] && x >= bufferX[1]) || (x >= bufferX[0] && x <= bufferX[1])) {
			if ((z <= bufferZ[0] && z >= bufferZ[1]) || (z >= bufferZ[0] && z <= bufferZ[1])) {
				return true;
			}
		}
		return false;
	}
	public boolean isInBuffer(Selection s) {
		double xLow = s.pos1.x;
		double xHigh = s.pos2.x;
		double zLow = s.pos1.z;
		double zHigh = s.pos2.z;
		if (s.pos2.x < xLow) {
			xLow = s.pos2.x;
			xHigh = s.pos1.x;
		}
		if (s.pos2.z < zLow) {
			zLow = s.pos2.z;
			zHigh = s.pos1.z;
		}
		for (double x = xLow; x <= xHigh; x++) {
			for (double z = zLow; z <= zHigh; z++) {
				if (isInBuffer(x, z)) return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines whether or not another Plot overlaps this one. This overload is really just for insurance.
	 * @param plot
	 * The Plot to check for overlap
	 * @return
	 * True if the specified Plot overlaps this one, else false
	 */
	public Boolean overlaps(Plot plot) {
		return overlaps(plot.getCorner1(),plot.getCorner2());
	}
	/**
	 * Determines whether or not an area overlaps this plot.
	 * @param corner1
	 * The first bounding coordinate of the area
	 * @param corner2
	 * The second bounding coordinate of the area
	 * @return
	 * True if the described area is in the same world and overlaps this Plot, else false.
	 */
	public Boolean overlaps(Location corner1, Location corner2) {
		if (corner1.getWorld().equals(corner2.getWorld()) && corner1.getWorld().equals(getCorner1().getWorld())) {
			double xLow = corner1.getBlockX();
			double xHigh = corner2.getBlockX();
			double zLow = corner1.getBlockZ();
			double zHigh = corner2.getBlockZ();
			if (corner2.getBlockX() < xLow) {
				xLow = corner2.getBlockX();
				xHigh = corner1.getBlockX();
			}
			if (corner2.getBlockZ() < zLow) {
				zLow = corner2.getBlockZ();
				zHigh = corner1.getBlockZ();
			}
			for (double x = xLow; x <= xHigh; x++) {
				for (double z = zLow; z <= zHigh; z++) {
					if (isInPlot(x, z)) return true;
				}
			}
		}
		return false;
	}
	public boolean overlaps(Selection s) {
		return overlaps(s.pos1.asLocation(),s.pos2.asLocation());
	}
	
	/**
	 * Wipes this plot based on the wipe settings for the City. Ignores plots that have at least 1 owner
	 */
	public void wipe() {
		World world = getAffiliation().getWorld();
		int xLow = getCorner1().getBlockX();
		int xHigh = getCorner2().getBlockX();
		int zLow = getCorner1().getBlockZ();
		int zHigh = getCorner2().getBlockZ();
		if (getCorner2().getBlockX() < xLow) {
			xLow = getCorner2().getBlockX();
			xHigh = getCorner1().getBlockX();
		}
		if (getCorner2().getBlockZ() < zLow) {
			zLow = getCorner2().getBlockZ();
			zHigh = getCorner1().getBlockZ();
		}
		
		for (int y = 1; y <= world.getMaxHeight(); y++) {
			for (int x = xLow; x <= xHigh; x++) {
				for (int z = zLow; z <= zHigh; z++) {
					Environment dimension = world.getEnvironment();
					Block block = world.getBlockAt(x, y, z);
					if (dimension == Environment.NETHER) {
						if (y < 5) block.setType(Material.BEDROCK);
						else if (y < 10) block.setType(Material.LAVA);
						else if (y < getBaseHeight()) block.setType(Material.NETHERRACK);
						else if (y < world.getMaxHeight() - 30) block.setType(Material.AIR);
						else if (y < world.getMaxHeight() - 20) block.setType(Material.NETHERRACK);
						else if (y < world.getMaxHeight() - 19) block.setType(Material.BEDROCK);
						else block.setType(Material.AIR);
					} else if (dimension == Environment.THE_END) {
						if (y < getBaseHeight() - 10) block.setType(Material.AIR);
						else if (y <= getBaseHeight()) block.setType(Material.END_STONE);
						else block.setType(Material.AIR);
					} else {
						if (y < 5) block.setType(Material.BEDROCK);
						else if (y < getBaseHeight() - 5) block.setType(Material.STONE);
						else if (y < getBaseHeight()) block.setType(Material.DIRT);
						else if (y == getBaseHeight()) block.setType(Material.GRASS);
						else block.setType(Material.AIR);
					}
				}
			}
		}
	}
	
	/**
	 * Compares plot corners to see if they're the same plot.
	 * It's impossible for plots to overlap, so if a plot's corners match, they must be the same.
	 * @param plot The plot to compare to this one
	 * @returns True if the plots have the same corners
	 */
	public Boolean equals(Plot plot) {
		return identifier == plot.getIdentifier() && affiliation.equals(plot.getAffiliation()); 
		
	}
	
	private String getProperty(String property) {
		String val = null;
		ConfigurationSection props = CityZen.cityConfig.getConfig().getConfigurationSection("cities." + getAffiliation().getIdentifier() + ".plots." + identifier);
		val = props.getString(property);
		return val;
	}
	
	private void setProperty(String property, Object value) {
		CityZen.cityConfig.getConfig().set("cities." + getAffiliation().getIdentifier() + ".plots." + identifier + "." + property,value);
	}
	
	private static int generateID(City city) {
		int id = 0;
		Boolean idChanged = false;
		Set<String> keys = CityZen.cityConfig.getConfig().getConfigurationSection("cities." + city.getIdentifier() + ".plots").getKeys(false);
		do {
			if (keys.contains(Integer.toString(id))) {
				id++;
			} else {
				idChanged = true;
			}
		} while (id > 0 && !idChanged);
		return id;
	}
}

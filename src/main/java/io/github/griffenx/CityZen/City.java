package io.github.griffenx.CityZen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class City implements Reputable {
	private String identifier;
	
	private ConfigurationSection properties;
	
	private static final CityLog log = CityZen.cityLog;
	
	/**
	 * Loads a new City into memory
	 * @param id
	 * The ID of the City to load
	 */
	private City(String name, Citizen founder) {
		for (City c : City.getCities()) {
			if (c.getName().equalsIgnoreCase(name)) return;
		}
		
		String id = generateID(name);
		identifier = id;
		CityZen.cityConfig.getConfig().createSection("cities." + id);
		properties = CityZen.cityConfig.getConfig().getConfigurationSection("cities." + id);
		FileConfiguration cnfg = CityZen.getPlugin().getConfig();
		ConfigurationSection defaults = cnfg.getConfigurationSection("cityDefaults");
		
		properties.set("name", name);
		properties.set("color", defaults.getString("color").charAt(1));
		properties.set("slogan", defaults.getString("slogan"));
		properties.set("freeJoin",defaults.getBoolean("freeJoin"));
		properties.set("openPlotting",defaults.getBoolean("openPlotting"));
		properties.set("wipePlots", defaults.getBoolean("wipePlots"));
		properties.set("naturalWipe",defaults.getBoolean("naturalWipe"));
		properties.set("blockBlacklist",defaults.getBoolean("blockBlacklist"));
		properties.set("useBlacklistAsWhitelist",defaults.getBoolean("useBlacklistAsWhitelist"));
		properties.set("maxPlotSize",cnfg.getInt("maxPlotSize"));
		properties.set("minPlotSize",cnfg.getInt("minPlotSize"));
		properties.set("founder", founder.getUUID().toString());
		properties.set("foundingDate", new SimpleDateFormat("yyyyMMdd").format(new Date()));
		if (founder.getPassport().isOnline()) properties.set("world", founder.getPlayer().getWorld().getName());
		else properties.set("world", "");
		properties.set("mayor", founder.getUUID().toString());
		properties.set("deputies", new Vector<String>());
		Vector<String> citizens = new Vector<String>();
		citizens.add(founder.getUUID().toString());
		properties.set("citizens", citizens);
		properties.set("banlist", new Vector<String>());
		properties.set("maxReputation", founder.getReputation());
		properties.set("protection", 2);
		properties.set("blacklistedBlocks", new Vector<String>());
		properties.set("waitlist", new Vector<String>());
		
		properties.createSection("plots");
		CityZen.cityConfig.save();
	}
	private City(String id) throws IllegalArgumentException {
		identifier = id;
		properties = CityZen.cityConfig.getConfig().getConfigurationSection("cities." + id);
		if (properties == null) throw new IllegalArgumentException("Attempted to get a City that doesn't exist");
	}
	
	/**
	 * Creates a new City in config based on the name given. Populates the City's properties with default values.
	 * @param name
	 * The name of the new City
	 * @return
	 * A brand new City, or {@literal null} if the City already exists
	 */
	public static City createCity(String name, Citizen founder) {
		if (getCity(name) != null) return null;
		City newCity = new City(name, founder);
		return newCity;
	}
	
	/**
	 * Gets all Cities from the config and returns them as a list
	 * @return
	 * A list of all defined Cities in the config
	 */
	public static List<City> getCities() {
		List<City> cities = new Vector<City>();
		ConfigurationSection citydata = CityZen.cityConfig.getConfig().getConfigurationSection("cities");
		for (String c : citydata.getKeys(false)) {
			cities.add(new City(c));
		}
		return cities;
	}
	
	public static City getCity(String name) {
		for (City c : getCities()) {
			if (c.getName().equalsIgnoreCase(name)) return c;
		}
		return null;
	}
	
	public static City getCity(Player player) {
		for (City c : getCities()) if (c.isInCity(player)) return c;
		return null;
	}
	
	public static City getCity(CommandSender sender) {
		if (sender instanceof Player) return getCity((Player)sender);
		else return null;
	}
	
	public static City getCity(Citizen citizen) {
		if (citizen.getPassport().isOnline()) {
			return getCity(citizen.getPlayer());
		} return null;
	}
	
	public static City getCity(Location location) {
		for (City c : getCities()) if (c.isInCity(location)) return c;
		return null;
	}
	/**
	 * Deletes this city from config
	 */
	public void delete() {
		CityZen.cityConfig.getConfig().set("cities." + identifier, null);
		CityZen.citizenConfig.save();
	}
	
	/**
	 * Simply gets this City's name
	 * @return
	 * This city's name
	 */
	public String getName() {
		return getProperty("name");
	}
	
	/**
	 * Sets the name of this city, does not change its ID
	 * @param name
	 * The new name to assign to this city
	 */
	public void setName(String name) {
		if (Character.isAlphabetic(name.charAt(0)) && name.length() < 50) {
			setProperty("name",name);
		}
	}
	
	/**
	 * Gets this City's identifier, making it a read-only property
	 * @return
	 * This City's identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Gets the Mayor of this City
	 * @return
	 * Citizen who is the Mayor of this city
	 */
	public Citizen getMayor() {
		String mayor = getProperty("mayor");
		//CityZen.getPlugin().getLogger().info("The UUID from file is: " + mayor);
		if (mayor != null && mayor.length() > 0) return Citizen.getCitizen(UUID.fromString(mayor));
		else return null;
	}
	
	/**
	 * Gets the Citizen who created this City
	 * @return
	 * The Citizen who founded this City
	 */
	public Citizen getFounder() {
		return Citizen.getCitizen(UUID.fromString(getProperty("founder")));
	}
	
	/* private void setFounder(Citizen founder) {
		setProperty("founder",founder.getUUID().toString());
	} */
	
	/**
	 * Gets the date that this City's record was created as a Date
	 * @return
	 * Date representing when this record was issued
	 */
	public Date getFoundingDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd",Locale.US);
		try {
			return sdf.parse(getProperty("foundingDate"));
		} catch (ParseException e) {
			return null;
		}
	}
	/**
	 * Gets the date that this City's record was created as a formatted String
	 * @param dateFormat
	 * A format string used to format the date.
	 * @return
	 * Date representing when this record was issued, as a string
	 */
	public String getFoundingDate(String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat,Locale.US);
		return sdf.format(getFoundingDate());
	}
	
	/* private void setFoundingDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd",Locale.US);
		setProperty("foundingDate",sdf.format(date));		
	} */
	
	/**
	 * Sets the mayor of this city to a new citizen. Overwrites the old mayor.
	 * @param newMayor
	 * The Citizen to set as the City's new Mayor
	 */
	public void setMayor(Citizen newMayor) {
		Citizen ctz = null;
		for (Citizen c : getCitizens()) {
			if (c.getPassport().getUniqueId().equals(newMayor.getPassport().getUniqueId())) {
				ctz = newMayor;
			}
		}
		if (ctz == null) {
			addCitizen(newMayor);
		}
		setProperty("mayor",newMayor.getPassport().getUniqueId().toString());
	}

	/**
	 * Simply gets this city's slogan
	 * @return
	 * This city's slogan
	 */
	public String getSlogan() {
		return getProperty("slogan");
	}
	
	/**
	 * Sets a new slogan for this city
	 * @param slogan
	 * The new slogan for this city
	 */
	public void setSlogan(String slogan) {
		if (Character.isAlphabetic(slogan.charAt(0)) && slogan.length() < 100) {
			setProperty("slogan",slogan);
		}
	}
	
	/**
	 * Gets the world in which this City resides based on its name in file
	 * @return
	 * The World in which this City resides
	 */
	public World getWorld() {
		return CityZen.getPlugin().getServer().getWorld(getProperty("world"));
	}
	
	/**
	 * Sets the World in which this City resides, but only if it does not have any plots. Should basically only be used in city creation.
	 * @param world
	 * The World in which this City resides
	 */
	public void setWorld(World world) {
		if (getPlots().size() > 0) {
			setProperty("world",world.getName());
		}
	}
	
	/**
	 * Gets the chat color for this City's name. If the value in config can't be converted to a color, it just uses WHITE instead.
	 * @return
	 * A ChatColor from this City's config
	 */
	public ChatColor getColor() {
		ChatColor color;
		String colorProperty = getProperty("color");
		if (colorProperty.length() > 0) {
			try {
				if (colorProperty.length() <= 2) color = ChatColor.getByChar(colorProperty.charAt(colorProperty.length() - 1));
				else color = ChatColor.valueOf(colorProperty);
				color = ChatColor.getByChar(getProperty("color").charAt(1));
			} catch(Exception e) {
				color = ChatColor.WHITE;
			}
		} else color = ChatColor.WHITE;
		return color;
	}
	
	/**
	 * Sets a new display chat color for this city. If a color code can't be gleaned from the character, this command does nothing.
	 * @param colorCharacter
	 * The color code character to assign to this city.
	 */
	public void setColor(char colorCharacter) {
		try {
			ChatColor color = ChatColor.getByChar(colorCharacter);
			if (color != null) setProperty("color","&" + colorCharacter);
		} catch (ClassCastException e) {}
	}
	
	/**
	 * Gets whether or not this City allows any player to join. Default: false. Defaults if property is set incorrectly.
	 * @return
	 * Whether or not this City allows FreeJoin
	 */
	public boolean isFreeJoin() {
		return Boolean.valueOf(getProperty("freeJoin"));
	}
	
	/**
	 * Sets whether or not any player can join this City
	 * @param state
	 * Whether or not freeJoin is allowed
	 */
	public void setFreeJoin(Boolean state) {
		setProperty("freeJoin",state);
	}
	
	/**
	 * Gets whether or not this City allows citizens to place plots. Defaults to false. Defaults if property is set incorrectly.
	 * @return
	 * Whether or not this City allows OpenPlotting
	 */
	public boolean isOpenPlotting() {
		return Boolean.valueOf(getProperty("openPlotting"));
	}
	
	/**
	 * Sets whether or not citizens of this city can create their own plots
	 * @param state
	 * Whether openPlotting is allowed
	 */
	public void setOpenPlotting(Boolean state) {
		setProperty("openPlotting",state);
	}
	
	public boolean isWipePlots() {
		return Boolean.valueOf(getProperty("wipePlots"));
	}
	
	public void setWipePlots(boolean state) {
		setProperty("wipePlots",state);
	}
	
	/**
	 * Gets whether or not this City is set to wipe plots to natural terrain from seed. Defaults to false (wipes to flatlands).
	 * Defaults if property is not set correctly.
	 * @returns
	 * Whether or not this City does NaturalWipe
	 */
	public boolean isNaturalWipe() {
		return Boolean.valueOf(getProperty("naturalWipe"));
	}
	
	/**
	 * Sets whether plots are wiped to natural terrain (from seed) or to flatlands.
	 * @param state
	 * Whether naturalWipe is enabled
	 */
	public void setNaturalWipe(Boolean state) {
		setProperty("naturalWipe",state);
	}
	
	/**
	 * Gets whether or not block restrictions are enabled. Defaults to false. Defaults if property is set incorrectly.
	 * @returns
	 * Whether or not this City uses block restrictions
	 */
	public boolean isBlockExclusion() {
		return Boolean.valueOf(getProperty("blockBlacklist"));
	}
	
	/**
	 * Whether block Blacklisting/Whitelisting is enabled
	 * @param state
	 * Whether or not blockExclusion is allowed
	 */
	public void setBlockExclusion(Boolean state) {
		setProperty("blockBlacklist",state);
	}
	
	/**
	 * Gets whether this City is using Whitelist or Blacklist exclusion mode. True = Whitelist, False = Blacklist.
	 * Defaults to false, and always returns false if block exclusion is disabled.
	 * @returns
	 * Whether this City uses Whitelist or Blacklist exclusion mode
	 */
	public boolean isWhitelisted() {
		return isBlockExclusion() && Boolean.valueOf(getProperty("useBlacklistAsWhitelist"));
	}
	
	/**
	 * Sets whether the exclusion mode of this city is Blacklist or Whitelist
	 * @param state
	 * True for Whitelist, False for Blacklist
	 */
	public void setWhitelisted(Boolean state) {
		setProperty("useBlacklistAsWhitelist",state);
	}
	
	/**
	 * Gets the maximum size of plots for this City. If the value in config is set to ignore (is less than 0),
	 * or otherwise can't be converted, the server default is used instead.
	 * @return
	 * The maximum plot size for this City
	 */
	public int getMaxPlotSize() {
		int maxPlotSize;
		try {
			maxPlotSize = Integer.valueOf(getProperty("maxPlotSize"));
		} catch (NumberFormatException e) {
			maxPlotSize = CityZen.getPlugin().getConfig().getInt("maxPlotSize");
		}
		if (maxPlotSize > -1) return maxPlotSize;
		else return CityZen.getPlugin().getConfig().getInt("maxPlotSize");
	}
	
	/**
	 * Sets the maximum plot size of this city. Must not be greater than the maximum size defined in config.yml
	 * @param size
	 * The new maximum plot size for this city
	 */
	public void setMaxPlotSize(int size) {
		int globalMax = CityZen.getPlugin().getConfig().getInt("maxPlotSize");
		if (size > globalMax) size = globalMax;
		setProperty("maxPlotSize",size);
	}
	
	/**
	 * Gets the minimum size of plots for this City. If the value in config is set to ignore (is less than 0),
	 * or otherwise can't be converted, the server default is used instead.
	 * @return
	 * The minimum plot size for this City
	 */
	public int getMinPlotSize() {
		int minPlotSize;
		try {
			minPlotSize = Integer.valueOf(getProperty("minPlotSize"));
		} catch (NumberFormatException e) {
			minPlotSize = CityZen.getPlugin().getConfig().getInt("minPlotSize");
		}
		if (minPlotSize > -1) return minPlotSize;
		else return CityZen.getPlugin().getConfig().getInt("minPlotSize");
	}
	
	/**
	 * Sets the minimum plot size of this city. Must not be less than the minimum size defined in config.yml
	 * @param size
	 * The new minimum plot size for this city
	 */
	public void setMinPlotSize(int size) {
		int globalMin = CityZen.getPlugin().getConfig().getInt("minPlotSize");
		if (size < globalMin) size = globalMin;
		setProperty("minPlotSize",size);
	}
	
	/**
	 * Gets a list of citizens from their UUID's in file. If a line cannot be converted, it will be ignored.
	 * @return
	 * A list of Citizens who are members of this City
	 */
	public List<Citizen> getCitizens() {
		List<Citizen> cits = new Vector<Citizen>();
		for (String u : CityZen.cityConfig.getConfig().getStringList("cities." + identifier + ".citizens")) {
			try {
				cits.add(Citizen.getCitizen(UUID.fromString(u)));
			} catch (IllegalArgumentException e) {}
		}
		return cits;
	}
	
	/**
	 * Adds a citizen to this city's list of citizens, then adds it back to the config
	 * @param ctz
	 * The citizen to add to this city
	 */
	public void addCitizen(Citizen ctz) {
		List<String> ctzs = new Vector<String>();
		for (Citizen c : getCitizens()) {
			ctzs.add(c.getPassport().getUniqueId().toString());
		}
		ctzs.add(ctz.getPassport().getUniqueId().toString());
		setProperty("citizens",ctzs);
		long rep = 0;
		rep = CityZen.getPlugin().getConfig().getLong("reputation.gainedOnJoinCity");
		ctz.addReputation(rep);
	}
	
	/**
	 * Remove a citizen from this city, and remove them from ownership of all plots
	 * @param ctz
	 * The citizen to remove
	 */
	public void removeCitizen(Citizen ctz) {
		removeCitizen(ctz, false);
	}
	/**
	 * Evict a citizen from this city, and remove them from ownership of all plots.
	 * Costs them more reputation if this is an eviction. Fails if the Citizen is the Mayor.
	 * @param ctz
	 * The citizen to evict
	 * @param evict
	 * Whether not this is an eviction
	 */
	public void removeCitizen(Citizen ctz, Boolean evict) {
		if (!ctz.isMayor()) {
			removeDeputy(ctz);
			List<String> ctzs = new Vector<String>();
			long reduction = 0;
			long minReduction = CityZen.getPlugin().getConfig().getLong("reputation.gainedOnCityJoin");
			for (Citizen c : getCitizens()) {
				if (c.equals(ctz)) {
					if (evict) {
						reduction = (long) (ctz.getReputation() * CityZen.getPlugin().getConfig().getInt("reputation.lostOnEvictionPercent") / 100.0);
						ban(ctz);
					} else {
						reduction = (long) (ctz.getReputation() * CityZen.getPlugin().getConfig().getInt("reputation.lostOnLeaveCityPercent") / 100.0);
					}
					if (reduction < minReduction) reduction = minReduction;
					ctz.subReputation(reduction);
					for (Plot p : getPlots()) {
						if (p.getOwners().contains(ctz)) {
							p.removeOwner(ctz);
							if (p.getOwners().size() == 0 && p.getAffiliation().isOpenPlotting() && p.getAffiliation().isWipePlots()) {
								p.wipe();
								p.delete();
							}
						}
					}
				}
				else {
					ctzs.add(c.getUUID().toString());
				}
			}
			setProperty("citizens",ctzs);
		}
	}
	
	/**
	 * Gets a list of deputies from their UUID's in file. If a line cannot be converted, it will be ignored.
	 * @return
	 * A list of Citizens who are deputies of this City
	 */
	public List<Citizen> getDeputies() {
		List<Citizen> deps = new Vector<Citizen>();
		for (String u : CityZen.cityConfig.getConfig().getStringList("cities." + identifier + ".deputies")) {
			try {
				deps.add(Citizen.getCitizen(UUID.fromString(u)));
			} catch (IllegalArgumentException e) {}
		}
		return deps;
	}
	
	/**
	 * Adds a Citizen to the list of Deputies for this City. Ignores players who are not a Citizen of the City or don't exist.
	 * @param deputy
	 * The player to add as a new Deputy
	 */
	public void addDeputy(Citizen deputy) {
		Citizen dep = null;
		for (Citizen c : getCitizens()) {
			if (c.equals(deputy)) {
				dep = deputy;
				break;
			}
		}
		if (dep != null) {
			List<String> deps = new Vector<String>();
			for (Citizen d : getDeputies()) {
				deps.add(d.getPassport().getUniqueId().toString());
			}
			deps.add(deputy.getPassport().getUniqueId().toString());
			setProperty("deputies",deps);
		}
	}
	
	/**
	 * Removes a player from the list of Deputies for this City
	 * @param deputy
	 * The player to remove from the Deputies list
	 */
	public void removeDeputy(Citizen deputy) {
		List<String> deps = new Vector<String>();
		for (Citizen d : getDeputies()) {
			if (!d.equals(deputy)) {
				deps.add(d.getPassport().getUniqueId().toString());
			}
			setProperty("deputies",deps);
		}
	}
	
	/**
	 * Gets a list of Citizens who have requested to join this City
	 * @return
	 * A List of Citizens who have requested to join this City. Returns null if freeJoin is false.
	 */
	public List<Citizen> getWaitlist() {
		List<Citizen> citizens = new Vector<Citizen>();
		if (!isFreeJoin()) {
			for (String u : properties.getStringList("waitlist")) {
				citizens.add(Citizen.getCitizen(UUID.fromString(u)));
			}
		}
		return citizens;
	}
	
	/**
	 * Adds a Citizen to this City's waitlist if they are not already on it.
	 * @param citizen
	 * The Citizen to add
	 */
	public void addWaitlist(Citizen citizen) {
		List<String> citizens = new Vector<String>();
		for (Citizen c : getWaitlist()) {
			if (c.equals(citizen)) return;
			citizens.add(c.getUUID().toString());
		}
		citizens.add(citizen.getUUID().toString());
		setProperty("waitlist", citizens);
	}
	
	/**
	 * Removes a Citizen from this City's waitlist
	 * @param citizen
	 * The Citizen to remove
	 */
	public void removeWaitlist(Citizen citizen) {
		List<String> citizens = new Vector<String>();
		for (Citizen c : getWaitlist()) if (!c.equals(citizen)) citizens.add(c.getUUID().toString());
		setProperty("waitlist", citizens);
	}
	
	/**
	 * Completely empties out this City's waitlist
	 */
	public void clearWaitlist() {
		setProperty("waitlist", new Vector<String>());
	}
	
	/**
	 * Gets a List of all Citizens who are on this City's banlist
	 * @return
	 * A List of all Citizens who are on this City's banlist
	 */
	public List<Citizen> getBanlist() {
		List<Citizen> banned = new Vector<Citizen>();
		for (String u : properties.getStringList("banlist")) {
			try {
				banned.add(Citizen.getCitizen(UUID.fromString(u)));
			} catch (IllegalArgumentException e) {
				log.write("Unable to parse player in banlist for city " + getName() + ": " + u);
			}
		}
		return banned;
	}
	
	/**
	 * Adds a Citizen to this City's banlist. If that Citizen is already banned, this method does nothing.
	 * @param citizen
	 * The Citizen to ban
	 */
	public void ban(Citizen citizen) {
		List<Citizen> banlist = getBanlist();
		if (!banlist.contains(citizen)) banlist.add(citizen);
		List<String> newBanlist = new Vector<String>();
		for (Citizen c : banlist) newBanlist.add(c.getUUID().toString());
		setProperty("banlist", newBanlist);
		
	}
	
	/**
	 * Removes a Citizen to this City's banlist. If that player is not banned, this method does nothing.
	 * @param citizen
	 * The Citizen to pardon.
	 */
	public void pardon(Citizen citizen) {
		if (getBanlist().contains(citizen)) {
			List<String> banlist = new Vector<String>();
			for (Citizen c : getBanlist()) if (!c.equals(citizen)) banlist.add(c.getUUID().toString());
			setProperty("banlist", banlist);
		}
	}
	
	/**
	 * Empties out this City's banlist. Doesn't give a damn whether it exists or not.
	 */
	public void clearBanlist() {
		setProperty("banlist", null);
	}
	
	/**
	 * Determines whether or not this Citizen is banned from this City.
	 * @param citizen
	 * The Citizen to check.
	 * @return
	 * True if this player is banned, else false.
	 */
	public boolean isBanned(Citizen citizen) {
		return getBanlist().contains(citizen);
	}
	
	/**
	 * Checks to see if the Citizen is on this City's waitlist
	 * @param citizen
	 * The Citizen to check
	 * @return
	 * True if the Citizen is on this City's waitlist, else false
	 */
	public boolean isInWaitlist(Citizen citizen) {
		for (Citizen c : getWaitlist()) {
			if (citizen.equals(c)) return true;
		}
		return false;
	}
	
	/**
	 * Gets a list of this City's plots.
	 * @return
	 * A List of this city's plots.
	 */
	public List<Plot> getPlots() {
		List<Plot> plts = new Vector<Plot>();
		ConfigurationSection cnfg = CityZen.cityConfig.getConfig().getConfigurationSection("cities." + getIdentifier() + ".plots");
		
		log.debug("Getting plots for city " + getName() + " (" + getIdentifier() + ") ");
		if (cnfg == null) {
			properties.createSection("plots");
			log.debug("Couldn't find plot section for this City. Creating config section at cities." + getIdentifier() + ".plots");
		}
		log.debug("Checking configuration section " + cnfg.getCurrentPath());
		
		log.debug("Here are all the keys in this City's plot section: " + cnfg.getKeys(false).toString());
		for (String key : cnfg.getKeys(false)) {
			log.debug("Adding plot stored at config location: " + cnfg.getCurrentPath() + "." + key);
			plts.add(Plot.getPlot(this,Integer.valueOf(key)));
		}
		log.debug("Returning " + plts.size() + " plot(s)");
		return plts;
	}
	
	/**
	 * Add a new Plot to this city using plot components. Fails if the created plot would overlap an existing plot.
	 * @param corner1
	 * The first corner of the plot
	 * @param corner2
	 * The second corner of the plot
	 * @param creator
	 * The Citizen who created this plot
	 */
	public void addPlot(Location corner1, Location corner2, Citizen creator) {
		Plot.createPlot(this, corner1, corner2, creator);
	}
	
	/**
	 * Checks a specific location for the existence of a plot and returns it.
	 * @param location
	 * The location to check.
	 * @return
	 * The Plot found at that location if one exists, else null
	 */
	public Plot getPlot(Location location) {
		List<Plot> plots = getPlots();
		
		log.debug("Received " + plots.size() + " plots");
		
		for (Plot p : plots) {
			if (p.isInPlot(location)) return p;
		} return null;
	}
	/**
	 * Checks a specific location for the existence of a plot and returns it.
	 * @param player
	 * The player whose location should be checked
	 * @return
	 * The Plot found at that location if one exists, else null
	 */
	public Plot getPlot(Player player) {
		return getPlot(player.getLocation());
	}
	/**
	 * Checks a specific location for the existence of a plot and returns it.
	 * @param sender
	 * The location associated with a Player CommandSender
	 * @return
	 * The Plot found at that location if one exists, else null. Returns null if sender is not an instance of a Player.
	 */
	public Plot getPlot(CommandSender sender) {
		if (sender instanceof Player) {
			return getPlot((Player)sender);
		} return null;
	}
	public Plot getPlot(int id) {
		for (Plot p : getPlots()) {
			if (p.getIdentifier() == id) {
				return p;
			}
		} return null;
	}
	
	/**
	 * Returns whether or not a set of X,Z coordinates are within the bounds of this City.
	 * @param x
	 * The X coordinate to check
	 * @param z
	 * The Z coordinate to check
	 * @return
	 * True if this coordinate is inside a plot or plot buffer for this City
	 */
	public boolean isInCity(double x, double z) {
		for (Plot p : getPlots()) {
			if (p.isInPlot(x, z) || p.isInBuffer(x, z)) return true;
		}
		return false;
	}
	/**
	 * Returns whether or not a Location object is within the bounds of this City.
	 * @param location
	 * The location to check.
	 * @return
	 * True if this location is inside a plot or plot buffer for this City
	 */
	public boolean isInCity(Location location) {
		return isInCity(location.getX(),location.getZ());
	}
	/**
	 * Returns whether or not a player is within the bounds of this City.
	 * @param player
	 * The player whose location should be checked
	 * @return
	 * True if this player is inside a plot or plot buffer for this City.
	 */
	public boolean isInCity(Player player) {
		return isInCity(player.getLocation());
	}
	/**
	 * Returns whether or not a Player CommandSender is within the bounds of this City.
	 * @param sender
	 * The CommandSender to check
	 * @return
	 * True if sender is an instance of a Player and that Player is within a Plot or plot buffer of this City.
	 */
	public boolean isInCity(CommandSender sender) {
		if (sender instanceof Player) {
			return isInCity((Player)sender);
		} return false;
	}
	
	/**
	 * Returns a list of materials that are blacklisted in this city. Illegible lines are ignored.
	 * @return
	 * A list of Materials that are blacklisted in this City.
	 */
	public List<Material> getBlacklist() {
		List<Material> mats = new Vector<Material>();
		for (String block : CityZen.cityConfig.getConfig().getStringList("cities." + identifier + ".blacklistedBlocks")) {
			Material mat = Material.getMaterial(block);
			if (mat != null) {
				mats.add(mat);
			}
		} return mats;
	}
	
	/**
	 * Add a block type to this city's exclusion list
	 * @param block
	 * The material type to add to the exclusion list
	 */
	public void addBlock(Material block) {
		List<String> mats = new Vector<String>();
		for (Material m : getBlacklist()) {
			mats.add(m.toString());
		}
		mats.add(block.toString());
		setProperty("blacklistedBlocks",mats);
	}
	
	/**
	 * Removes a block type from this city's exclusion list
	 * @param block
	 * The material type to remove from this city's exclusion list
	 */
	public void removeBlock(Material block) {
		List<String> mats = new Vector<String>();
		for (Material m : getBlacklist()) {
			if (m != block) mats.add(m.toString());
		}
		setProperty("blacklistedBlocks",mats);
	}
	
	/**
	 * Returns the center point of this city, based on its plots.
	 * @return
	 * A Location that signifies the center of this city
	 */
	public Position getCenter() {
		Position center = null;
		if (getPlots().size() > 0) {
			Double maxX = null,
				minX = null,
				maxZ = null,
				minZ = null;
			for (Plot p : getPlots()) {
				maxX = null;
				minX = null;
				maxZ = null;
				minZ = null;
				if (maxX == null || p.getCorner1().getX() > maxX) maxX = p.getCorner1().getX();
				if (minX == null || p.getCorner1().getX() < minX) minX = p.getCorner1().getX();
				if (maxZ == null || p.getCorner1().getZ() > maxZ) maxZ = p.getCorner1().getZ();
				if (minZ == null || p.getCorner1().getZ() < minZ) minZ = p.getCorner1().getZ();
				
				if (p.getCorner2().getX() > maxX) maxX = p.getCorner2().getX();
				if (p.getCorner2().getX() < minX) minX = p.getCorner2().getX();
				if (p.getCorner2().getZ() > maxZ) maxZ = p.getCorner2().getZ();
				if (p.getCorner2().getZ() < minZ) minZ = p.getCorner2().getZ();
			}
			if (maxX != null && minX != null && maxZ != null && minZ != null) center = new Position(getPlots().get(0).getCorner1().getWorld(),(maxX + minX) / 2, 0, (maxZ + minZ) / 2);
		}
		return center;
	}
	
	/**
	 * Puts the city's color and name together to get a chat-friendly representation of its name
	 * @return
	 * Color + Name
	 */
	public String getChatName() {
		return getColor() + getName() + ChatColor.RESET;
	}

	/**
	 * Gets this city's reputation as the sum of its citizens
	 * @return
	 * This city's reputation
	 */
	public long getReputation() {
		long tot = 0;
		for(Citizen c : getCitizens()) tot += c.getReputation();
		return tot;
	}
	
	/**
	 * Send an Alert message to all Citizens of this City
	 * @param alertText
	 * The message of the Alert
	 */
	public void alertCitizens(String alertText) {
		for (Citizen c : getCitizens()) {
			c.addAlert(alertText);
		}
	}
	
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
		if (level >=0 && level < 3) {
			setProperty("protection",level);
			for (Plot p : getPlots()) {
				if (p.isMega()) p.setProtectionLevel(level);
			}
		}
		else setProperty("protection",2);
	}
	
	/**
	 * Sets the protection level for Plot buffers in this City
	 * @param level
	 * The exact protection level to set
	 */
	public void setProtectionLevel(ProtectionLevel level) {
		setProtectionLevel(ProtectionLevel.getIndex(level));
	}
	
	@Override
	public long getMaxReputation() {
		long maxRep = properties.getLong("maxReputation");
		if (maxRep < 0) {
			maxRep = 0;
		}
		return maxRep;
	}
	
	public void setMaxReputation(long amount) {
		if (amount < 0) amount = 0;
		setProperty("maxReputation", amount);
	}
	
	@Override
	public void sendReward(Reward r) {
		if (r.getIsBroadcast()) {
			CityZen.getPlugin().getServer().broadcastMessage(r.getFormattedString(r.getMessage(), this));
		}
		for (Citizen c : getCitizens()) {
			if (c.getPassport().isOnline()) {
				CityZen.getPlugin().getServer().dispatchCommand(CityZen.getPlugin().getServer().getConsoleSender(),
						r.getFormattedString(r.getCommand(),c));
				c.sendMessage(r.getFormattedString(r.getMessage(),this));
			} else c.queueReward(r);
		}
	}

	@Override
	public List<Reward> getRewards() {
		List<Reward> rewards = new Vector<Reward>();
        for (Reward r : getRewards()) {
            if (r.getType().equals("c")) {
                if (getMaxReputation() == r.getInitialRep() || 
                    (getMaxReputation() - r.getInitialRep()) % r.getIntervalRep() == 0) rewards.add(r);
            }
        } return rewards;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		City city = (City)obj;
		return identifier.equalsIgnoreCase(city.getIdentifier());
	}

	private String getProperty(String property) {
		// Get the property from the city's config
		String val = properties.getString(property);
		if (val == null) val = CityZen.getPlugin().getConfig().getString("cityDefaults." + property);
		//CityZen.getPlugin().getLogger().info("Properties for the selected City: " + properties.toString() + "\nSelected property: " + property + "\nFound Value: " + val);
		return val;
	}
	
	private void setProperty(String property, Object value) {
		CityZen.cityConfig.getConfig().set("cities." + identifier + "." + property, value);
	}
	
	private static String generateID(String name) {
		String id = "";
		for (int i = 0; i < name.length(); i++) {
			if (Character.isAlphabetic(name.charAt(i))) id += name.charAt(i);
		}
		
		Boolean idChanged = false;
		int modifier = 0;
		Set<String> keys = CityZen.cityConfig.getConfig().getConfigurationSection("cities").getKeys(false);
		do {
			if (keys.contains(id + (modifier != 0 ? modifier : "")) && !(getCity(id + (modifier != 0 ? modifier : "")).getName().equalsIgnoreCase(name))) {
				modifier++;
			}
			else {
				if (modifier > 0) id += modifier;
				idChanged = true;
			}
		}
		while (modifier > 0 && !idChanged);
		return id;
	}

}

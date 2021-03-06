package io.github.griffenx.CityZen.Commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import io.github.griffenx.CityZen.Citizen;
import io.github.griffenx.CityZen.City;
import io.github.griffenx.CityZen.CityZen;
import io.github.griffenx.CityZen.Messaging;
import io.github.griffenx.CityZen.Plot;
import io.github.griffenx.CityZen.Position;
import io.github.griffenx.CityZen.ProtectionLevel;
import io.github.griffenx.CityZen.Selection;
import io.github.griffenx.CityZen.Util;
import io.github.griffenx.CityZen.Tasks.ClearMetadataTask;

public class PlotCommand {
	private static FileConfiguration config = CityZen.getPlugin().getConfig();

	public static boolean delegate(CommandSender sender, String[] args) {
		if (args.length > 0)
			switch (args[0].toLowerCase()) {
			case "list":
				list(sender, args);
				break;
			case "price":
				price(sender, args);
				break;
			case "avail":
			case "available":
				available(sender, args);
				break;
			case "buy":
				buy(sender);
				break;
			case "claim":
				claim(sender);
				break;
			case "del":
			case "destroy":
			case "delete":
				delete(sender, args);
				break;
			case "leave":
			case "abandon":
				abandon(sender);
				break;
			case "1":
			case "p1":
			case "pos1":
			case "2":
			case "p2":
			case "pos2":
			case "sel":
			case "select":
			case "c":
			case "cr":
			case "cre":
			case "crea":
			case "creat":
			case "create":
			case "m":
			case "mo":
			case "mov":
			case "move":
				select(sender, args);
				break;
			case "invite":
				invite(sender, args);
				break;
			case "add":
			case "addowner":
			case "remove":
			case "removeowner":
				modifyowners(sender, args);
				break;
			case "i":
			case "info":
				info(sender);
				break;
			case "prot":
			case "protection":
			case "setprotection":
				setprotection(sender, args);
				break;
			case "accept":
			case "deny":
				inviteReply(sender, args);
				break;
			case "wipe":
				wipe(sender);
				break;
			default:
				sender.sendMessage(Messaging.noSuchSubcommand(args[0]));
				return false;
			}
		else
			sender.sendMessage(Messaging.noArguments("plot"));
		return true;
	}

	private static void select(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.select")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					final Player user = (Player) sender;
					final City city = citizen.getAffiliation();
					if (city != null) {
						if (city.isOpenPlotting() || citizen.isCityOfficial())
							switch (args[0].toLowerCase()) {
							case "s":
							case "se":
							case "sel":
							case "sele":
							case "selec":
							case "select":
								if (citizen.getPlayer().hasMetadata("plotSelectEnabled")) {
									citizen.getPlayer().removeMetadata("plotSelectEnabled", CityZen.getPlugin());
									citizen.getPlayer().removeMetadata("pos1", CityZen.getPlugin());
									citizen.getPlayer().removeMetadata("pos2", CityZen.getPlugin());
									sender.sendMessage(ChatColor.BLUE + "Plot selection disabled.");
								} else {
									citizen.getPlayer().setMetadata("plotSelectEnabled",
											new FixedMetadataValue(CityZen.getPlugin(), true));
									sender.sendMessage(ChatColor.BLUE
											+ "Plot selection enabled. Select corners with \"/plot pos1\" and \"/plot pos2\". "
											+ "Disable plot selection with \"/plot select\"");
								}
								break;
							case "1":
							case "p1":
							case "pos1":
								if (citizen.getPlayer().hasMetadata("plotSelectEnabled")) {
									// Format: world;x;y;z
									final Location pos = new Location(user.getLocation().getWorld(),
											user.getLocation().getX(), user.getLocation().getY() - 1,
											user.getLocation().getZ());
									for (final City c : City.getCities())
										for (final Plot p : c.getPlots())
											if (p.isInPlot(pos)) {
												sender.sendMessage(ChatColor.RED
														+ "This location is inside an existing plot. Try a different location.");
												return;
											}
									if (citizen.getPlayer().hasMetadata("pos2")) {
										final World w = CityZen.getPlugin().getServer().getWorld(citizen.getPlayer()
												.getMetadata("pos2").get(0).asString().split(";")[0]);
										if (!w.equals(user.getWorld()))
											citizen.getPlayer().removeMetadata("pos2", CityZen.getPlugin());
									}
									final String newPos = user.getWorld().getName() + ";"
											+ Integer.toString(pos.getBlockX()) + ";"
											+ Integer.toString(pos.getBlockY()) + ";"
											+ Integer.toString(pos.getBlockZ());
									citizen.getPlayer().setMetadata("pos1",
											new FixedMetadataValue(CityZen.getPlugin(), newPos));
									sender.sendMessage(ChatColor.BLUE + "Position 1 set (" + newPos + ")");
								} else
									sender.sendMessage(ChatColor.RED
											+ "Please enable plot selection with \"/plot select\" before selecting corners.");
								break;
							case "2":
							case "p2":
							case "pos2":
								if (citizen.getPlayer().hasMetadata("plotSelectEnabled")) {
									// Format: world;x;y;z
									final Location pos2 = new Location(user.getLocation().getWorld(),
											user.getLocation().getX(), user.getLocation().getY() - 1,
											user.getLocation().getZ());
									for (final City c : City.getCities())
										for (final Plot p : c.getPlots())
											if (p.isInPlot(pos2)) {
												sender.sendMessage(ChatColor.RED
														+ "This location is inside an existing plot. Try a different location.");
												return;
											}
									if (citizen.getPlayer().hasMetadata("pos1")) {
										final World w = CityZen.getPlugin().getServer().getWorld(citizen.getPlayer()
												.getMetadata("pos1").get(0).asString().split(";")[0]);
										if (!w.equals(user.getWorld()))
											citizen.getPlayer().removeMetadata("pos1", CityZen.getPlugin());
									}
									final String newPos2 = user.getWorld().getName() + ";"
											+ Integer.toString(pos2.getBlockX()) + ";"
											+ Integer.toString(pos2.getBlockY()) + ";"
											+ Integer.toString(pos2.getBlockZ());
									citizen.getPlayer().setMetadata("pos2",
											new FixedMetadataValue(CityZen.getPlugin(), newPos2));
									sender.sendMessage(ChatColor.BLUE + "Position 2 set (" + newPos2 + ")");
								} else
									sender.sendMessage(ChatColor.RED
											+ "Please enable plot selection with \"/plot select\" before selecting corners.");
								break;
							case "c":
							case "cr":
							case "cre":
							case "crea":
							case "creat":
							case "create":
								create(citizen, city, sender, args);
								break;
							case "m":
							case "mo":
							case "mov":
							case "move":
								Plot plot = null;
								if (args.length == 1) {
									if (citizen.getPlots().size() != 1) {
										sender.sendMessage(Messaging.notEnoughArguments("/plot move <ID>"));
										return;
									}
									plot = citizen.getPlots().get(0);
								} else {
									int id = -1;
									try {
										id = Integer.valueOf(args[1]);
									} catch (final NumberFormatException e) {
										sender.sendMessage(ChatColor.RED + "Plot ID must be a number.");
										return;
									}
									if (id >= 0)
										plot = city.getPlot(id);
								}
								if (plot != null) {
									if (plot.getOwners().contains(citizen) || citizen.isCityOfficial())
										move(citizen, city, sender, plot, args);
									else
										sender.sendMessage(Messaging.notPlotOwner());
								} else
									sender.sendMessage(ChatColor.RED + "No plot found with ID " + args[1]);
								break;
							}
						else
							sender.sendMessage(ChatColor.RED + city.getName()
									+ " does not allow OpenPlotting, meaning only City officials can create Plots.");
					} else
						sender.sendMessage(Messaging.noAffiliation());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.select"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void create(Citizen citizen, City city, CommandSender sender, String[] args) {
		if (sender.hasPermission("cityzen.plot.create")) {
			if (citizen.getPlots().size() < citizen.getMaxPlots() || citizen.isCityOfficial()) {
				if (city.getPlots().size() < Math.round(city.getCitizens().size() * config.getDouble("plotDensity"))) {
					if (citizen.getPlayer().hasMetadata("pos1") && citizen.getPlayer().hasMetadata("pos2")) {
						final Position position1 = new Position(
								citizen.getPlayer().getMetadata("pos1").get(0).asString());
						final Position position2 = new Position(
								citizen.getPlayer().getMetadata("pos2").get(0).asString());
						final Selection sel = new Selection(position1, position2);
						if (!sel.worldGuardConflicts(citizen)) {
							if (position1.world.equals(position2.world)) {
								if (position1.world.equals(city.getWorld())) {
									boolean isMega = false;
									if (sel.getArea() > Math.pow(city.getMaxPlotSize(), 2)
											|| sel.getArea() > Math.pow(config.getDouble("maxPlotSize"), 2)) {
										if (!citizen.isCityOfficial()) {
											sender.sendMessage(ChatColor.RED
													+ "You cannot create a Plot this big. Max Plot Size: "
													+ city.getMaxPlotSize() + " (City), "
													+ config.getDouble("maxPlotSize") + " (Server)\n"
													+ "(Plot size factor is based on the width of a square Plot)");
											return;
										}
										if (sel.getArea() <= Math.pow(config.getDouble("maxMegaPlotSize"), 2)) {
											isMega = true;
											sender.sendMessage(ChatColor.BLUE
													+ "This Plot is too large for a standard Plot and will be created as a MegaPlot.");
										} else {
											sender.sendMessage(ChatColor.RED
													+ "This Plot is too large for both a standard and Mega Plot. Please select a smaller area.");
											return;
										}
									}
									if (sel.getArea() >= Math.pow(city.getMinPlotSize(), 2)
											&& sel.getArea() >= Math.pow(config.getDouble("minPlotSize"), 2)) {
										final int minWidth = config.getInt("minPlotWidth");
										if (sel.getSideX() >= minWidth && sel.getSideZ() >= minWidth) {
											if (city.getPlots().size() != 0) {
												boolean inPlotBuffer = false;
												for (final City c : City.getCities())
													for (final Plot p : c.getPlots()) {
														if (p.overlaps(sel)) {
															sender.sendMessage(ChatColor.RED
																	+ "You cannot create a new Plot here. It overlaps another Plot.");
															return;
														}
														if (p.isInBuffer(sel.getBuffer(1)))
															if (c.equals(city))
																inPlotBuffer = true;
															else {
																sender.sendMessage(ChatColor.RED
																		+ "You cannot create a new Plot here. It overlaps "
																		+ c.getName());
																return;
															}
													}
												if (inPlotBuffer) {
													final Plot newPlot = Plot.createEmptyPlot(city,
															position1.asLocation(), position2.asLocation(), citizen);
													if (isMega) {
														newPlot.setMega(true);
														newPlot.setProtectionLevel(city.getProtectionLevel());
													} else if (city.isOpenPlotting())
														newPlot.addOwner(citizen);
													if (city.isWipePlots() && !(args.length >= 2
															&& args[1].equalsIgnoreCase("preserve")))
														newPlot.wipe();
													sender.sendMessage(ChatColor.BLUE
															+ "A new Plot was sucessfully created for "
															+ city.getChatName() + ChatColor.BLUE
															+ " at the selected location.\n"
															+ "This area is now protected from interference by non-owners. Type \""
															+ ChatColor.GOLD + "/cityzen help plot management"
															+ ChatColor.BLUE
															+ "\" for ways to customize your new Plot.");
													citizen.getPlayer().removeMetadata("plotSelectEnabled",
															CityZen.getPlugin());
													citizen.getPlayer().removeMetadata("pos1", CityZen.getPlugin());
													citizen.getPlayer().removeMetadata("pos2", CityZen.getPlugin());
												} else
													sender.sendMessage(ChatColor.RED
															+ "You cannot create a Plot here. Plots must be no more than "
															+ config.getInt("plotBuffer")
															+ " blocks away from another Plot buffer in the same City.");
											} else if (citizen.isCityOfficial()) {
												if (sel.isSpaced(city)) {
													final Plot newPlot = Plot.createEmptyPlot(city,
															position1.asLocation(), position2.asLocation(), citizen);
													if (isMega) {
														newPlot.setMega(true);
														newPlot.setProtectionLevel(city.getProtectionLevel());
													} else if (city.isOpenPlotting())
														newPlot.addOwner(citizen);
													if (city.isWipePlots() && !(args.length >= 2
															&& args[1].equalsIgnoreCase("preserve")))
														newPlot.wipe();
													sender.sendMessage(ChatColor.BLUE
															+ "A new Plot was sucessfully created for "
															+ city.getChatName() + ChatColor.BLUE
															+ " at the selected location.\n"
															+ "This area is now protected from interference by non-owners. Type \""
															+ ChatColor.GOLD + "/cityzen help plot management"
															+ ChatColor.BLUE
															+ "\" for ways to customize your new Plot.");
													citizen.getPlayer().removeMetadata("plotSelectEnabled",
															CityZen.getPlugin());
													citizen.getPlayer().removeMetadata("pos1", CityZen.getPlugin());
													citizen.getPlayer().removeMetadata("pos2", CityZen.getPlugin());
												} else
													sender.sendMessage(ChatColor.RED
															+ "This City is not far away enough from another City. Cities must be "
															+ CityZen.getPlugin().getConfig()
																	.getInt("minCitySeparation")
															+ " blocks away from one another and must not overlap.");
											} else
												sender.sendMessage(ChatColor.RED
														+ "This City has no plots yet. Let a City official set the first plot.");
										} else
											sender.sendMessage(ChatColor.RED
													+ "You cannot create a Plot of this size. Both the length and the width of all Plots must be at least "
													+ config.getInt("minPlotWidth") + " blocks each.");
									} else
										sender.sendMessage(
												ChatColor.RED + "You cannot create a Plot this small. Min Plot Size: "
														+ city.getMinPlotSize() + " (City), "
														+ config.getDouble("minPlotSize") + " (Server)\n"
														+ "(Plot size factor is based on the width of a square Plot)");
								} else
									sender.sendMessage(ChatColor.RED
											+ "You cannot create a Plot in a different world than the rest of the City.");
							} else
								sender.sendMessage(ChatColor.RED
										+ "You must select two points in the same world to create a Plot.");
						} else
							sender.sendMessage(Messaging.worldGuardConflicts());
					} else
						sender.sendMessage(
								ChatColor.RED + "You must select two corners with \"/plot pos1\" and \"/plot pos2\".");
				} else
					sender.sendMessage(ChatColor.RED + city.getName() + " cannot have any more Plots.");
			} else
				sender.sendMessage(ChatColor.RED + "You cannot have any more Plots.");
		} else
			sender.sendMessage(Messaging.noPerms("cityzen.plot.create"));
	}

	private static void move(Citizen citizen, City city, CommandSender sender, Plot plot, String[] args) {
		if (sender.hasPermission("cityzen.plot.move")) {
			if (citizen.getPlots().size() <= citizen.getMaxPlots() || citizen.isCityOfficial()) {
				if (city.getPlots().size() <= Math.round(city.getCitizens().size() * config.getDouble("plotDensity"))) {
					if (citizen.getPlayer().hasMetadata("pos1") && citizen.getPlayer().hasMetadata("pos2")) {
						final Position position1 = new Position(
								citizen.getPlayer().getMetadata("pos1").get(0).asString());
						final Position position2 = new Position(
								citizen.getPlayer().getMetadata("pos2").get(0).asString());
						final Selection sel = new Selection(position1, position2);
						if (!sel.worldGuardConflicts(citizen)) {
							if (position1.world.equals(position2.world)) {
								if (position1.world.equals(city.getWorld())) {
									if (sel.getArea() > Math.pow(city.getMaxPlotSize(), 2)
											|| sel.getArea() > Math.pow(config.getDouble("maxPlotSize"), 2)) {
										if (!plot.isMega() || !citizen.isCityOfficial()) {
											sender.sendMessage(ChatColor.RED
													+ "The area you have selected is too large. Only city officials can move MegaPlots to large areas.");
											return;
										}
										if (sel.getArea() > Math.pow(config.getDouble("maxMegaPlotSize"), 2)) {
											sender.sendMessage(
													ChatColor.RED + "This selection is too big for even a MegaPlot.");
											return;
										}
									}
									if (sel.getArea() >= Math.pow(city.getMinPlotSize(), 2)
											&& sel.getArea() >= Math.pow(config.getDouble("minPlotSize"), 2)) {
										final int minWidth = config.getInt("minPlotWidth");
										if (sel.getSideX() >= minWidth && sel.getSideZ() >= minWidth) {
											boolean inPlotBuffer = false;
											for (final City c : City.getCities())
												for (final Plot p : c.getPlots()) {
													if (p != plot && p.overlaps(sel)) {
														sender.sendMessage(ChatColor.RED
																+ "You cannot create a new Plot here. It overlaps another Plot.");
														return;
													}
													if (p.isInBuffer(sel.getBuffer(1)))
														if (c.equals(city))
															inPlotBuffer = true;
														else {
															sender.sendMessage(ChatColor.RED
																	+ "You cannot create a new Plot here. It overlaps "
																	+ c.getName());
															return;
														}
												}
											if (inPlotBuffer) {
												if (city.isWipePlots()
														&& !(args.length >= 2 && args[1].equalsIgnoreCase("preserve")))
													plot.wipe();

												plot.setCorner1(position1.asLocation());
												plot.setCorner2(position2.asLocation());

												if (city.isWipePlots()
														&& !(args.length >= 2 && args[1].equalsIgnoreCase("preserve")))
													plot.wipe();
												sender.sendMessage(ChatColor.BLUE
														+ "The plot was successfully moved to the selected location.\n"
														+ "This area is now protected from interference by non-owners. Type \""
														+ ChatColor.GOLD + "/cityzen help plot management"
														+ ChatColor.BLUE + "\" for ways to customize your new Plot.");
												citizen.getPlayer().removeMetadata("plotSelectEnabled",
														CityZen.getPlugin());
												citizen.getPlayer().removeMetadata("pos1", CityZen.getPlugin());
												citizen.getPlayer().removeMetadata("pos2", CityZen.getPlugin());
											} else
												sender.sendMessage(ChatColor.RED
														+ "You cannot create a Plot here. Plots must be no more than "
														+ config.getInt("plotBuffer")
														+ " blocks away from another Plot in the same City.");
										} else
											sender.sendMessage(ChatColor.RED
													+ "You cannot create a Plot of this size. Both the length and the width of all Plots must be at least "
													+ config.getInt("minPlotWidth") + " blocks each.");
									} else
										sender.sendMessage(
												ChatColor.RED + "You cannot create a Plot this small. Min Plot Size: "
														+ city.getMinPlotSize() + " (City), "
														+ config.getDouble("minPlotSize") + " (Server)\n"
														+ "(Plot size factor is based on the width of a square Plot)");
								} else
									sender.sendMessage(ChatColor.RED
											+ "You cannot create a Plot in a different world than the rest of the City.");
							} else
								sender.sendMessage(ChatColor.RED
										+ "You must select two points in the same world to create a Plot.");
						} else
							sender.sendMessage(Messaging.worldGuardConflicts());
					} else
						sender.sendMessage(
								ChatColor.RED + "You must select two corners with \"/plot pos1\" and \"/plot pos2\".");
				} else
					sender.sendMessage(ChatColor.RED + city.getName() + " cannot have any more Plots.");
			} else
				sender.sendMessage(ChatColor.RED + "You cannot have any more Plots.");
		} else
			sender.sendMessage(Messaging.noPerms("cityzen.plot.move"));
	}

	private static void list(CommandSender sender, String[] args) {
		if (sender.hasPermission("cityzen.plot.list")) {
			Citizen citizen = null;
			if (args.length > 1 && sender.hasPermission("cityzen.plot.list.others")) {
				citizen = Citizen.getCitizen(args[1]);
				if (citizen == null) {
					sender.sendMessage(Messaging.citizenNotFound(args[1]));
					return;
				}
			} else {
				citizen = Citizen.getCitizen(sender);
				if (citizen == null) {
					sender.sendMessage(Messaging.missingCitizenRecord());
					return;
				}
			}

			if (citizen.getAffiliation() != null && citizen.getPlots().size() > 0) {
				final StringBuilder message = new StringBuilder(ChatColor.BLUE + "Plots owned by: " + ChatColor.GOLD
						+ citizen.getName() + ChatColor.BLUE + ":\n");
				for (final Plot p : citizen.getPlots()) {
					message.append(ChatColor.BLUE + "| " + p.getIdentifier() + ": (" + (int) p.getCorner1().getX() + ","
							+ (int) p.getCorner1().getZ() + "),(" + (int) p.getCorner2().getX() + ","
							+ (int) p.getCorner2().getZ() + ") Height: " + p.getBaseHeight() + " Protection: "
							+ p.getProtectionLevel() + "\n|   Creator: " + p.getCreator().getName() + " Co-Owners:");
					for (final Citizen c : p.getOwners())
						if (!citizen.equals(c))
							message.append(" " + c.getName());
					message.append('\n');
				}
				sender.sendMessage(message.toString());
			} else
				sender.sendMessage(ChatColor.RED + "No plots owned by " + citizen.getName());
		} else
			sender.sendMessage(Messaging.noPerms("cityzen.plot.list"));
	}

	private static void price(CommandSender sender, String[] args) {
		if (config.getBoolean("useEconomy")) {
			if (sender instanceof Player) {
				final Citizen citizen = Citizen.getCitizen(sender);
				Plot plot = null;
				for (final City c : City.getCities()) {
					for (final Plot p : c.getPlots())
						if (p.isInPlot(citizen.getPlayer().getLocation())) {
							plot = p;
							break;
						}
					if (plot != null)
						break;
				}

				if (plot == null) {
					sender.sendMessage(ChatColor.RED + "You must be inside a plot to run this command");
					return;
				}
				if (args.length == 1) {
					if (sender.hasPermission("cityzen.plot.price")) {
						if (plot.isMega() || (plot.getOwners().size() > 0 && plot.getPrice() == 0))
							sender.sendMessage(ChatColor.RED + "This plot is not for sale.");
						else
							sender.sendMessage(ChatColor.BLUE + "Price: " + ChatColor.GOLD + plot.getPrice());
					} else
						sender.sendMessage(Messaging.noPerms("cityzen.plot.price"));
				} else if (args[1].equalsIgnoreCase("set")) {
					if (sender.hasPermission("cityzen.plot.price.set")) {
						if (args.length > 2) {
							if (!plot.isMega() || plot.getOwners().contains(citizen)
									|| ((citizen.isMayor() || citizen.isDeputy())
											&& citizen.getAffiliation().equals(plot.getAffiliation()))
									|| sender.hasPermission("cityzen.plot.price.set.others"))
								try {
									plot.setPrice(Double.valueOf(args[2]));
									sender.sendMessage(
											ChatColor.BLUE + "The price of this plot is now " + plot.getPrice() + "."
													+ (plot.getPrice() == 0 ? " This plot is not for sale." : ""));

								} catch (final NumberFormatException e) {
									sender.sendMessage(ChatColor.RED + "Price must be a number.");
								}
							else
								sender.sendMessage(ChatColor.RED + "You cannot set a price for this plot.");
						} else
							sender.sendMessage(Messaging.notEnoughArguments("/plot price set <price>"));
					} else
						sender.sendMessage(Messaging.noPerms("cityzen.plot.price.set"));
				} else
					sender.sendMessage(Messaging.invalidArguments("/plot price (set) (price)"));
			} else
				sender.sendMessage(Messaging.playersOnly());
		} else
			sender.sendMessage(Messaging.econDisabled());
	}

	private static void buy(CommandSender sender) {
		if (config.getBoolean("useEconomy")) {
			if (sender.hasPermission("cityzen.plot.buy")) {
				if (sender instanceof Player) {
					final Player user = (Player) sender;
					final Citizen citizen = Citizen.getCitizen(sender);
					if (citizen != null) {
						if (citizen.getAffiliation() != null) {
							Plot plot = null;
							for (final Plot p : citizen.getAffiliation().getPlots())
								if (p.isInPlot(user.getLocation())) {
									plot = p;
									break;
								}
							if (plot != null) {
								if (citizen.getPlots().size() < citizen.getMaxPlots()) {
									if (plot.getPrice() > 0) {
										if (CityZen.econ.getBalance(CityZen.getPlugin().getServer()
												.getOfflinePlayer(citizen.getUUID())) >= plot.getPrice()) {
											citizen.addReputation(config.getLong("reputation.gainedOnBuyPlot"));
											final List<Citizen> formerOwners = plot.getOwners();
											for (final Citizen o : formerOwners) {
												o.sendMessage(ChatColor.BLUE + "Your plot centered at "
														+ plot.getCenter() + " was sold to " + citizen.getName()
														+ " for " + plot.getPrice() + " "
														+ CityZen.econ.currencyNamePlural());
												CityZen.econ.depositPlayer(
														CityZen.getPlugin().getServer().getOfflinePlayer(o.getUUID()),
														plot.getPrice() / formerOwners.size());
												o.subReputation(config.getLong("reputation.lostOnSellPlot"));
												plot.removeOwner(o);
											}
											CityZen.econ.withdrawPlayer(
													CityZen.getPlugin().getServer().getOfflinePlayer(citizen.getUUID()),
													plot.getPrice());
											plot.addOwner(citizen);
											sender.sendMessage(ChatColor.BLUE
													+ "You successfully purchased this plot for " + plot.getPrice()
													+ " " + CityZen.econ.currencyNamePlural());
											plot.setPrice(0);
										} else
											sender.sendMessage(ChatColor.RED + "You cannot afford this plot. Price: "
													+ plot.getPrice() + CityZen.econ.currencyNamePlural()
													+ " You have: "
													+ CityZen.econ.getBalance(CityZen.getPlugin().getServer()
															.getOfflinePlayer(citizen.getUUID()))
													+ CityZen.econ.currencyNamePlural());
									} else
										sender.sendMessage(ChatColor.RED + "This plot is not for sale.");
								} else
									sender.sendMessage(ChatColor.RED + "You have reached your maximum plot limit of "
											+ citizen.getMaxPlots()
											+ ". Get rid of plots or increase your limit before acquiring more.");
							} else
								sender.sendMessage(Messaging.noPlotFound());
						} else
							sender.sendMessage(Messaging.noAffiliation());
					} else
						sender.sendMessage(Messaging.missingCitizenRecord());
				} else
					sender.sendMessage(Messaging.playersOnly());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.buy"));
		} else
			sender.sendMessage(Messaging.econDisabled());
	}

	private static void available(CommandSender sender, String[] args) {
		if (sender.hasPermission("cityzen.plot.available")) {
			City city = null;
			if (args.length == 1) {
				if (sender instanceof Player) {
					final Citizen citizen = Citizen.getCitizen(sender);
					if (citizen != null) {
						if (citizen.getAffiliation() != null)
							city = citizen.getAffiliation();
						else
							sender.sendMessage(Messaging.noAffiliation());
					} else
						sender.sendMessage(Messaging.missingCitizenRecord());
				} else
					sender.sendMessage(Messaging.playersOnly());
			} else if (sender.hasPermission("cityzen.plot.available.others")) {
				city = City.getCity(Util.findCityName(args));
				if (city == null) {
					sender.sendMessage(Messaging.cityNotFound());
					return;
				}
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.available.others"));

			if (city != null)
				if (!city.isOpenPlotting()) {
					final StringBuilder plotsList = new StringBuilder();
					int i = 1;
					for (final Plot p : city.getPlots())
						if (p.getOwners().isEmpty() && !p.isMega()) {
							plotsList.append(ChatColor.BLUE + "| " + i + ". (" + p.getCenter().getBlockX() + ","
									+ p.getCenter().getBlockZ() + ")\n");
							i++;
						}
					if (i == 1)
						sender.sendMessage(ChatColor.RED + "No available plots found in " + city.getName()
								+ ". Ask a city official to create some more.");
					else
						sender.sendMessage(new String[] { ChatColor.BLUE + "Open plots in " + city.getChatName() + ":",
								plotsList.toString() });
				} else
					sender.sendMessage(ChatColor.BLUE + "OpenPlotting is enabled in " + city.getChatName()
							+ ". Empty plots will automatically wipe. Feel free to create your own plot.");
		} else
			sender.sendMessage(Messaging.noPerms("cityzen.plot.available"));
	}

	private static void claim(CommandSender sender) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.claim")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					final City city = citizen.getAffiliation();
					if (city != null) {
						if (!city.isOpenPlotting()) {
							Plot plot = null;
							for (final Plot p : city.getPlots())
								if (p.isInPlot(citizen.getPlayer().getLocation()))
									plot = p;
							if (plot != null) {
								if (plot.getOwners().size() == 0) {
									if (!plot.isMega() || citizen.isCityOfficial())
										if (citizen.getPlots().size() < citizen.getMaxPlots()) {
											plot.addOwner(citizen);
											final long rep = config.getLong("reputation.gainedOnClaimPlot");
											citizen.addReputation(rep);
											citizen.sendMessage(
													ChatColor.BLUE + "You successfully claimed this plot, gaining "
															+ ChatColor.GOLD + rep + " Reputation.");
										} else
											sender.sendMessage(Messaging.tooManyPlots());
								} else
									sender.sendMessage(ChatColor.RED
											+ "This plot is currently owned, meaning it is not available to claim.");
							} else
								sender.sendMessage(
										ChatColor.RED + "You must be inside a plot in your city to use this command.");
						} else
							sender.sendMessage(ChatColor.RED + city.getName()
									+ " has OpenPlotting enabled. Empty plots are automatically wiped, so there's no need to claim them.");
					} else
						sender.sendMessage(Messaging.noAffiliation());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.claim"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void delete(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.delete")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					City city = citizen.getAffiliation();
					if (sender.hasPermission("cityzen.plot.delete.others"))
						for (final City c : City.getCities())
							if (c.isInCity((Player) sender))
								city = c;
					if (city != null) {
						final Plot plot = city.getPlot(sender);
						if (plot != null) {
							if (sender.hasPermission("cityzen.plot.delete.others") || citizen.isCityOfficial()
									|| plot.getOwners().contains(citizen)) {
								final long rep = config.getLong("reputation.lostOnLeavePlot");
								for (final Citizen c : plot.getOwners()) {
									c.sendMessage(ChatColor.BLUE + "A plot you own centered at ("
											+ plot.getCenter().getBlockX() + "," + plot.getCenter().getBlockZ()
											+ ") was deleted by " + sender.getName() + ". You lost " + ChatColor.GOLD
											+ rep + " Reputation.");
									c.subReputation(rep);
								}
								if (city.isWipePlots() && !(args.length >= 2 && args[1].equalsIgnoreCase("preserve")))
									plot.wipe();
								if (!plot.getOwners().contains(citizen))
									sender.sendMessage(ChatColor.BLUE + "You successfully deleted this plot.");
								plot.delete();
							} else
								sender.sendMessage(
										ChatColor.RED + "You don't have permission to delete this plot. Only Admins, "
												+ "city officials, and owners of the plot can delete it.");
						} else
							sender.sendMessage(Messaging.noPlotFound());
					} else
						sender.sendMessage(Messaging.noAffiliation());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.delete"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void abandon(CommandSender sender) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.abandon")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					final City city = citizen.getAffiliation();
					if (city != null) {
						final Plot plot = city.getPlot(sender);
						if (plot != null) {
							if (plot.getOwners().contains(citizen)) {
								final long rep = config.getLong("reputation.lostOnLeavePlot");
								for (final Citizen c : plot.getOwners())
									if (!c.equals(citizen))
										c.sendMessage(ChatColor.BLUE + citizen.getName()
												+ " left ownership of your plot centered at ("
												+ plot.getCenter().getBlockX() + "," + plot.getCenter().getBlockZ()
												+ ").");

								plot.removeOwner(citizen);

								boolean isAbandoned = false;
								if (plot.getOwners().size() == 0) {
									if (city.isWipePlots())
										plot.wipe();
									if (city.isOpenPlotting())
										plot.delete();
									else
										isAbandoned = true;
								}
								citizen.subReputation(rep);
								sender.sendMessage(ChatColor.BLUE + "You successfully abandoned this plot."
										+ (isAbandoned ? " This plot is now abandoned, meaning anyone can claim it."
												: "")
										+ " You lost " + ChatColor.GOLD + rep + " Reputation.");
							} else
								sender.sendMessage(
										ChatColor.RED + "You cannot abandon this plot, since you don't own it.");
						} else
							sender.sendMessage(Messaging.noPlotFound());
					} else
						sender.sendMessage(Messaging.noAffiliation());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.abandon"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void wipe(CommandSender sender) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.wipe")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					City city = citizen.getAffiliation();
					if (sender.hasPermission("cityzen.plot.wipe.others"))
						for (final City c : City.getCities())
							if (c.isInCity((Player) sender))
								city = c;
					if (city != null) {
						final Plot plot = city.getPlot(sender);
						if (plot != null) {
							if (sender.hasPermission("cityzen.plot.wipe.others") || citizen.isCityOfficial()
									|| plot.getOwners().contains(citizen)) {
								plot.wipe();
								if (!plot.getOwners().contains(citizen))
									sender.sendMessage(ChatColor.BLUE + "You successfully wiped this plot.");
								for (final Citizen c : plot.getOwners())
									c.sendMessage(ChatColor.BLUE + "Your plot centered at " + plot.getCenterCoords()
											+ " was wiped by " + citizen.getName());
							} else
								sender.sendMessage(
										ChatColor.RED + "You don't have permission to wipe this plot. Only Admins, "
												+ "city officials, and owners of the plot can wipe it.");
						} else
							sender.sendMessage(Messaging.noPlotFound());
					} else
						sender.sendMessage(Messaging.noAffiliation());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.wipe"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void invite(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.invite")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					final City city = citizen.getAffiliation();
					if (city != null) {
						final Plot plot = city.getPlot(sender);
						if (plot != null) {
							if (plot.getOwners().contains(citizen)) {
								if (args.length > 1) {
									final Citizen target = Citizen.getCitizen(args[1]);
									if (target != null) {
										if (citizen.getAffiliation().equals(target.getAffiliation())) {
											if (!plot.getOwners().contains(target)) {
												if (target.getPlots().size() < target.getMaxPlots()) {
													if (target.getPlayer().isOnline()) {
														if (!target.getPlayer().hasMetadata("plotInvite")) {
															target.getPlayer().setMetadata("plotInvite",
																	new FixedMetadataValue(CityZen.getPlugin(),
																			citizen.getName() + ";"
																					+ plot.getIdentifier()));
															new ClearMetadataTask(target.getPlayer(), "plotInvite")
																	.runTaskLater(CityZen.getPlugin(), 20 * 120);
															sender.sendMessage(ChatColor.BLUE + "Invite sent to "
																	+ target.getPlayer().getDisplayName()
																	+ ChatColor.BLUE
																	+ ". You will be notified if they accept.");
															target.sendMessage(ChatColor.BLUE
																	+ "You have a new plot invite from "
																	+ citizen.getPlayer().getDisplayName() + "!\n"
																	+ "Type \"" + ChatColor.GOLD + "/plot accept"
																	+ ChatColor.BLUE + "\" or \"" + ChatColor.WHITE
																	+ "/plot deny" + ChatColor.BLUE + "\"\n"
																	+ "This invite will expire in 2 minutes.");
														} else
															sender.sendMessage(ChatColor.RED
																	+ target.getPlayer().getDisplayName()
																	+ ChatColor.RED
																	+ " already has a pending invite. Please try again later.");
													} else {
														target.sendMessage(citizen.getName()
																+ " wanted to invite you to a plot, but you were offline.");
														sender.sendMessage(ChatColor.BLUE + target.getName()
																+ " was offline, but they were notified that you want to add them to your plot. Try again when they're online.");
													}
												} else
													sender.sendMessage(ChatColor.RED + target.getName()
															+ " cannot own any more plots.");
											} else
												sender.sendMessage(ChatColor.RED + target.getName()
														+ " is already an owner of this plot.");
										} else
											sender.sendMessage(ChatColor.RED
													+ "The invited player must be a Citizen of the same City as you.");
									} else
										sender.sendMessage(Messaging.citizenNotFound(args[1]));
								} else
									sender.sendMessage(Messaging.notEnoughArguments("/plot invite <Citizen>"));
							} else
								sender.sendMessage(Messaging.notPlotOwner());
						} else
							sender.sendMessage(Messaging.noPlotFound());
					} else
						sender.sendMessage(Messaging.noAffiliation());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.invite"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void inviteReply(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if ((args[0].substring(0, 1).equalsIgnoreCase("a") && sender.hasPermission("cityzen.plot.accept"))
					|| (args[0].substring(0, 1).equalsIgnoreCase("d") && sender.hasPermission("cityzen.plot.deny"))) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					final City city = citizen.getAffiliation();
					if (city != null) {
						if (citizen.getPlots().size() < citizen.getMaxPlots()) {
							if (citizen.getPlayer().hasMetadata("plotInvite")) {
								final String[] invite = citizen.getPlayer().getMetadata("plotInvite").get(0).asString()
										.split(";");
								final Citizen host = Citizen.getCitizen(invite[0]);
								if (host != null) {
									citizen.getPlayer().removeMetadata("plotInvite", CityZen.getPlugin());
									if (args[0].substring(0, 1).equalsIgnoreCase("a")) {
										if (city.equals(host.getAffiliation())) {
											final Plot plot = Plot.getPlot(city, Integer.parseInt(invite[1]));
											if (plot != null) {
												if (plot.getOwners().contains(host)) {
													if (!plot.getOwners().contains(citizen)) {
														plot.addOwner(citizen);
														citizen.sendMessage(ChatColor.BLUE
																+ "You have sucessfully become an owner of this Plot!");
														host.sendMessage(ChatColor.BLUE
																+ citizen.getPlayer().getDisplayName() + ChatColor.BLUE
																+ " accepted your Plot invitation."
																+ " They are now an owner of your Plot!");
													} else
														sender.sendMessage(ChatColor.RED
																+ "You are already an owner of this Plot");
												} else
													sender.sendMessage(ChatColor.RED + host.getName()
															+ " no longer owns this Plot.");
											} else
												sender.sendMessage(Messaging.noPlotFound());
										} else
											sender.sendMessage(ChatColor.RED + "You must be a Citizen of "
													+ host.getAffiliation().getName() + " to join this Plot.");
									} else if (args[0].substring(0, 1).equalsIgnoreCase("d")) {
										sender.sendMessage(ChatColor.BLUE + "Invitation denied.");
										host.sendMessage(
												ChatColor.RED + citizen.getName() + " denied your Plot invitation.");
									}
								} else
									sender.sendMessage(ChatColor.RED
											+ "Could not respond to your invitation. The Citizen record of " + invite[0]
											+ " couldn't be found.");
							} else
								sender.sendMessage(ChatColor.RED + "You have not been invited to any Plots.");
						} else
							sender.sendMessage(Messaging.tooManyPlots());
					} else
						sender.sendMessage(Messaging.noAffiliation());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms(
						(args[0].substring(0, 1).equalsIgnoreCase("a") ? "cityzen.plot.accept" : "cityzen.plot.deny")));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void modifyowners(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.modifyowners")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					City city = citizen.getAffiliation();
					if (sender.hasPermission("cityzen.plot.modifyowners.others"))
						for (final City c : City.getCities())
							if (c.isInCity((Player) sender))
								city = c;
					if (city != null) {
						final Plot plot = city.getPlot(sender);
						if (plot != null) {
							if (plot.getOwners().contains(citizen) || citizen.isCityOfficial()
									|| sender.hasPermission("cityzen.plot.modifyowners.others")) {
								if (args.length > 1) {
									final Citizen target = Citizen.getCitizen(args[1]);
									if (target != null) {
										if (args[0].toLowerCase().contains("add")) {
											if (!plot.getOwners().contains(target)) {
												if (target.getPlots().size() < target.getMaxPlots()) {
													for (final Citizen c : plot.getOwners())
														c.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.BLUE
																+ " was added to your plot centered at ("
																+ plot.getCenter().getBlockX() + ","
																+ plot.getCenter().getBlockZ() + ") by "
																+ sender.getName());
													plot.addOwner(target);
													target.sendMessage(
															ChatColor.BLUE + "You were added to a plot centered at ("
																	+ plot.getCenter().getBlockX() + ","
																	+ plot.getCenter().getBlockZ() + ") by "
																	+ sender.getName());
													sender.sendMessage(ChatColor.BLUE + "Successfully added "
															+ target.getName() + " t this plot.");
												} else
													sender.sendMessage(ChatColor.RED + target.getName()
															+ " cannot own any more plots.");
											} else
												sender.sendMessage(
														ChatColor.RED + target.getName() + " already owns this plot.");
										} else if (plot.getOwners().contains(target)) {
											plot.removeOwner(target);
											for (final Citizen c : plot.getOwners())
												c.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.BLUE
														+ " was removed from your plot centered at ("
														+ plot.getCenter().getBlockX() + ","
														+ plot.getCenter().getBlockZ() + ") by " + sender.getName());
											target.sendMessage(ChatColor.BLUE
													+ "You were removed from a plot centered at ("
													+ plot.getCenter().getBlockX() + "," + plot.getCenter().getBlockZ()
													+ ") by " + sender.getName());
											sender.sendMessage(ChatColor.BLUE + "Successfully removed "
													+ target.getName() + " from this plot.");
										} else
											sender.sendMessage(
													ChatColor.RED + "This plot is not owned by " + target.getName());
									} else
										sender.sendMessage(Messaging.citizenNotFound(args[1]));
								} else
									sender.sendMessage(Messaging.notEnoughArguments("/plot " + args[0] + " <Citizen>"));
							} else
								sender.sendMessage(Messaging.notCityOfficial());
						} else
							sender.sendMessage(Messaging.noPlotFound());
					} else
						sender.sendMessage(Messaging.cityNotFound());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.modifyowners"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void info(CommandSender sender) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.info")) {
				CityZen.cityLog.debug("Sender location: " + ((Player) sender).getLocation().getBlockX() + ","
						+ ((Player) sender).getLocation().getBlockZ());
				final Plot plot = Plot.getPlot(((Player) sender).getLocation());
				if (plot != null) {
					final StringBuilder message = new StringBuilder();
					message.append(ChatColor.BLUE + "This Plot:\n");
					message.append(ChatColor.BLUE + "Affiliation: " + plot.getAffiliation().getChatName() + " ID: "
							+ plot.getIdentifier() + "\n");
					if (plot.isMega())
						message.append(ChatColor.BLUE + "MegaPlot\n");
					message.append(ChatColor.BLUE + "Corner 1: (" + ChatColor.WHITE + plot.getCorner1().getBlockX()
							+ ChatColor.BLUE + "," + ChatColor.WHITE + plot.getCorner1().getBlockZ() + ChatColor.BLUE
							+ ")\n");
					message.append(ChatColor.BLUE + "Corner 2: (" + ChatColor.WHITE + plot.getCorner2().getBlockX()
							+ ChatColor.BLUE + "," + ChatColor.WHITE + plot.getCorner2().getBlockZ() + ChatColor.BLUE
							+ ")\n");
					message.append(ChatColor.BLUE + "Center: " + ChatColor.WHITE + plot.getCenterCoords() + "\n");
					message.append(ChatColor.BLUE + "Size: " + ChatColor.WHITE + (int) Math.sqrt(plot.getArea())
							+ ChatColor.BLUE + " Area: " + ChatColor.WHITE + plot.getArea().intValue() + "\n");
					message.append(ChatColor.BLUE + "Base Height:" + ChatColor.WHITE + plot.getBaseHeight() + "\n");
					message.append(ChatColor.BLUE + "Protection: " + ChatColor.WHITE
							+ plot.getProtectionLevel().toString() + "\n");
					message.append(ChatColor.BLUE + "Owners:\n");
					for (final Citizen o : plot.getOwners())
						message.append(ChatColor.BLUE + "    - " + o.getName() + "\n");
					message.append(ChatColor.BLUE + "Creator: " + plot.getCreator().getName() + "\n");
					if (config.getBoolean("useEconomy") && plot.getPrice() > 0)
						message.append(ChatColor.BLUE + "Price: " + ChatColor.GREEN + plot.getPrice() + " "
								+ CityZen.econ.currencyNamePlural() + "\n");
					sender.sendMessage(message.toString());
				} else
					sender.sendMessage(Messaging.noPlotFound());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.info"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}

	private static void setprotection(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("cityzen.plot.setprotection")) {
				final Citizen citizen = Citizen.getCitizen(sender);
				if (citizen != null) {
					final Plot plot = City.getCity(sender).getPlot(sender);
					if (plot != null) {
						if (plot.getOwners().contains(citizen) || citizen.isCityOfficial()
								|| sender.hasPermission("cityzen.plot.setprotection.others")) {
							if (args.length > 1)
								switch (args[1].toLowerCase()) {
								case "0":
								case "none":
								case "pu":
								case "pub":
								case "public":
									plot.setProtectionLevel(ProtectionLevel.PUBLIC);
									sender.sendMessage(
											ChatColor.BLUE + "You set the protection level for this plot to PUBLIC. "
													+ "Any player can now build in this plot.");
									break;
								case "1":
								case "c":
								case "co":
								case "com":
								case "comm":
								case "communal":
									plot.setProtectionLevel(ProtectionLevel.COMMUNAL);
									sender.sendMessage(
											ChatColor.BLUE + "You set the protection level for this plot to COMMUNAL. "
													+ "Any Citizen of this City can now build in this plot.");
									break;
								case "2":
								case "p":
								case "pr":
								case "pro":
								case "prot":
								case "protected":
									plot.setProtectionLevel(ProtectionLevel.PROTECTED);
									sender.sendMessage(
											ChatColor.BLUE + "You set the protection level for this plot to PROTECTED. "
													+ "Only City officials can now build in this plot.");
									break;
								default:
									sender.sendMessage(ChatColor.RED + "\"" + args[1]
											+ "\" is not a protection level. Valid levels: public, communal, protected.");
								}
							else
								sender.sendMessage(Messaging.notEnoughArguments("/plot setprotection <level>"));
						} else
							sender.sendMessage(Messaging.notPlotOwner());
					} else
						sender.sendMessage(Messaging.noPlotFound());
				} else
					sender.sendMessage(Messaging.missingCitizenRecord());
			} else
				sender.sendMessage(Messaging.noPerms("cityzen.plot.setprotection"));
		} else
			sender.sendMessage(Messaging.playersOnly());
	}
}

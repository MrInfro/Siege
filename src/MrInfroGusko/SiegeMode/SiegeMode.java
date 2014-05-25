package MrInfroGusko.SiegeMode;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class SiegeMode
  extends JavaPlugin
{
  static final Logger log = Logger.getLogger("Minecraft");
  public static Permission permission = null;
  Date date = new Date();
  Calendar calendar = GregorianCalendar.getInstance();
  
  public void onEnable()
  {
    setupPermission();
    this.saveDefaultConfig();
    getConfig();
    PluginDescriptionFile pdfFile = getDescription();
    log.log(Level.INFO, "[{0}] By MrInfro and Gusko - v{1} enabled.", new Object[] { pdfFile.getName(), pdfFile.getVersion() });
    log.log(Level.INFO, "[{0}] Command execution will start in {1} seconds.", new Object[] { pdfFile.getName(), Integer.valueOf(getConfig().getInt("InitialDelay")) });
    int DayOfWeek = getDayOfWeek();
    String parsedDayOfWeek = parseDayOfWeek(DayOfWeek);
    log.log(Level.INFO, "[SiegeMode] The day of the week is {0}", parsedDayOfWeek);
    initialDelay();
  }
  
  public void onDisable()
  {
    PluginDescriptionFile pdfFile = getDescription();
    log.log(Level.INFO, "[{0}] By MrInfro and Gusko - v{1} disabled.", new Object[] { pdfFile.getName(), pdfFile.getVersion() });
    getServer().getScheduler().cancelTasks(this);
  }
  
  public void initialDelay()
  {
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    {
      public void run()
      {
        SiegeMode.log.info("[SiegeMode] has started executing commands");
        SiegeMode.log.info("-------------[SiegeMode]--------------");
        SiegeMode.this.startSchedule();
      }
    }, getConfig().getInt("InitialDelay") * 20L);
  }
  
  public void startSchedule()
  {
    int counter = 1;
    int started = 0;
    while (getConfig().contains("SiegeSchedule.startCity" + counter))
    {    
        if (getConfig().getInt("SiegeSchedule.startCity" + counter + ".Day")==getDayOfWeek()){
            timeTask(counter, "startCity");
            // log entry for debug purposes mostly
            log.log(Level.INFO, "[SiegeMode] Start of siege event on City" + counter);
        }
      started++;
      counter++;
    }    
    counter = 1;
    while (getConfig().contains("SiegeSchedule.endCity" + counter))
    {
        if (getConfig().getInt("SiegeSchedule.endCity" + counter + ".Day")==getDayOfWeek()){
            timeTask(counter, "endCity");
            // log entry for debug purposes mostly
            log.log(Level.INFO, "[SiegeMode] End of siege event on City" + counter);
        }
        started++;
        counter++;
    }
    log.log(Level.INFO, "[SiegeMode] has found {0} records.", Integer.valueOf(started));
  }
    
  public void timeTask(final int counter, final String cityType)
  {
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
      public void run()
      {
        SiegeMode.this.runCommand(counter, cityType);
      }
    }, getTime(counter, cityType) * 20L, 1728000L);
  }
  
  public void runCommand(int counter, String cityType)
  {
      int subCounter = 1;

      while (getConfig().contains("SiegeSchedule." + cityType + counter + ".Command" + subCounter)) {
      getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("SiegeSchedule." + cityType + counter + ".Command" + subCounter));
      subCounter++;
      }
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    PluginDescriptionFile desc = getDescription();
    if (cmd.getName().equalsIgnoreCase("sm"))
    {
      if (args.length == 0)
      {
        msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.WHITE + "To see a list of commands type: /sm help", new Object[0]);
        return true;
      }
// commented as we want anyone to use most options, kept here for informational purposes
//      if (!hasPerm(sender, "SiegeMode.use"))
//      {
//        msg(sender, ChatColor.RED + "You don't have permission to use this command", new Object[0]);
//        return true;
//      }
      if (args[0].equalsIgnoreCase("reload") && hasPerm(sender, "SiegeMode.use"))
      {
        msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.WHITE + "Reloding...", new Object[0]);
        reloadConfig();
        msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.GREEN + "Reloaded config file!", new Object[0]);
        return true;
      }
//    if (!hasPerm(sender, "SiegeMode.use"))
//    {
//      msg(sender, ChatColor.RED + "You don't have permission to use this command", new Object[0]);
//      return true;
//    }
	if (args[0].equalsIgnoreCase("siege"))
	{
	msg(sender, ChatColor.RED + "[SiegeMode]" + ChatColor.GREEN + "List of cities & sieges", new Object[0]);
	int counter = 1;
    while (getConfig().contains("SiegeSchedule.startCity" + counter))
    {    
                String CityName = getConfig().getString("SiegeSchedule.startCity" + counter + ".CityName");
                String Day = parseDayOfWeek(getConfig().getInt("SiegeSchedule.startCity" + counter + ".Day"));
                int Hour = getConfig().getInt("SiegeSchedule.startCity" + counter + ".Hour");
                int Minute = getConfig().getInt("SiegeSchedule.startCity" + counter + ".Minute");
		msg(sender, ChatColor.WHITE + CityName + " " + Day + " " + Hour + " " + Minute,new Object[0]);
		counter++;
    }    
	}
    if (args[0].equalsIgnoreCase("help"))
    {
      msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.WHITE + "Commands:", new Object[] { desc.getVersion() });
      msg(sender, ChatColor.WHITE + "* /sm siege - displays list of cities and their siege times", new Object[0]);
      msg(sender, ChatColor.WHITE + "* (admin only) /sm reload", new Object[0]);
      msg(sender, ChatColor.WHITE + "* (admin only) /sm setSiege cityNumber hour minute", new Object[0]);
      return true;
    }
	if ((args[0].equalsIgnoreCase("setSiege"))&&(hasPerm(sender, "SiegeMode.use")))
	  {

		//validace zda tam vubec sou hodnoty... zatim se nekontroluje co za picoviny sou tam napsane ale bude
		//POZOR nez zmaknu prechody do dalsiho dne, musime mit ENUM od 0.00 do 21.59 (2 hodiny do pulnoci)
		if ((!args[1].isEmpty() || !args[2].isEmpty())||!args[3].isEmpty())
            	{
			try {
                		getConfig().set("SiegeSchedule.startCity" + args[1] + ".Hour", Integer.parseInt(args[2]));
				getConfig().set("SiegeSchedule.startCity" + args[1] + ".Minute", Integer.parseInt(args[3]));
				//int endHour = Integer.parseInt(args[2])+2;
				//String endHour = Integer.toString((parseInt(args[3]))+2);
				getConfig().set("SiegeSchedule.endCity" + args[1] + ".Hour", Integer.parseInt(args[2])+2);
				getConfig().set("SiegeSchedule.endCity" + args[1] + ".Minute", Integer.parseInt(args[3]));
                		log.log(Level.INFO, "[SiegeMode] Time of siege was changed for City" + Integer.parseInt(args[1]));
				this.saveConfig();
				this.reloadConfig();
				return true;
			} catch (Exception ex) {
				getLogger().log(Level.SEVERE, "[SIEGEMODE] se to komplet umrelo na setConfig", ex);
			}
            	}
        	  else
	          {
            		msg(sender, ChatColor.RED + "[SiegeMode] u fail!", new Object[0]);
            		log.log(Level.INFO, "[SiegeMode] siege time input fail on primary args check");
            return false;
          }
	}
    }
    return false;
    }
  
  private boolean hasPerm(CommandSender sender, String perm)
  {
    if ((perm == null) || (perm.equals(""))) {
      return true;
    }
    if (!(sender instanceof Player)) {
      return true;
    }
    Player player = (Player)sender;
    return player.hasPermission(perm);
  }
  
  private boolean setupPermission()
  {
    PluginManager pm = getServer().getPluginManager();
    if (pm.getPlugin("Vault") != null)
    {
      @SuppressWarnings("rawtypes")
	RegisteredServiceProvider permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
      if (permissionProvider != null) {
        permission = (Permission)permissionProvider.getProvider();
      }
      log.info("[SiegeMode] Vault Found! Hooking as permission system");
      return permission != null;
    }
    permission = null;
    log.info("[SiegeMode] Vault Not Found");
    log.info("[SiegeMode] Defaulting to SuperPerms");
    return false;
  }
  
  public void msg(CommandSender sender, String msg, Object[] objects)
  {
    msg = MessageFormat.format(msg, objects);
    if (!(sender instanceof Player)) {
      log.info(msg.replaceAll("&([0-9a-fk-or])", ""));
    }
    if ((sender instanceof Player)) {
      sender.sendMessage(msg.replaceAll("&([0-9a-fk-or])", "§$1"));
    }
  }
  
  public int getTime(int counter, String cityType)
  {
    this.calendar.setTime(this.date);
    
    int time_in_seconds = this.calendar.get(11) * 3600 + this.calendar.get(12) * 60 + this.calendar.get(13);
    int time_wanted = getConfig().getInt("SiegeSchedule." + cityType + counter + ".Hour", 0) * 3600 + getConfig().getInt("SiegeSchedule." + cityType + counter + ".Minute", 0) * 60 + getConfig().getInt("SiegeSchedule." + cityType + counter + ".Second", 0);
    int time;
    if (time_wanted >= time_in_seconds) {
      time = time_wanted - time_in_seconds;
    } else {
      time = 86400 + time_wanted - time_in_seconds;
    }
    return time;
  }
  
  public int getDayOfWeek()
  {
	  calendar.setFirstDayOfWeek(calendar.getFirstDayOfWeek());
	  int DayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK);

	  return DayOfTheWeek; 
  }
  
  public String parseDayOfWeek(int DayOfWeek)
  {
	  String parsedDayOfWeek = "";
	  switch (DayOfWeek)
	  {
	  	case 2:
	  		parsedDayOfWeek = "MONDAY";
	  		break;
	  	case 3:
	  		parsedDayOfWeek = "TUESDAY";
	  		break;
	  	case 4:
	  		parsedDayOfWeek = "WEDNESDAY";
	  		break;
	  	case 5:
	  		parsedDayOfWeek = "THURSDAY";
	  		break;
	  	case 6:
	  		parsedDayOfWeek = "FRIDAY";
	  		break;
	  	case 7:
	  		parsedDayOfWeek = "SATURDAY";
	  		break;
	  	case 1:
	  		parsedDayOfWeek = "SUNDAY";
	  		break;
	  }
	  return parsedDayOfWeek;
  }
}

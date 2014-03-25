package MrInfroGusko.SiegeMode;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
//import java.util.GregorianCalendar;
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
  Calendar calendar = Calendar.getInstance();
  
  public void onEnable()
  {
    setupPermission();
    getConfig().options().copyDefaults(true);
    saveConfig();
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
            log.log(Level.INFO, "[SiegeMode] Nacten zacatek siege pro city" + counter);
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
            log.log(Level.INFO, "[SiegeMode] Nacten konec siege pro city" + counter);
        }
        started++;
        counter++;
    }
    log.log(Level.INFO, "[SiegeMode] has attempted to put {0} commands on schedule.", Integer.valueOf(started));
  }
  
  // OLD (kept for record, delete once done)
  /*public void startSchedule()
  {
    int counter = 1;
    int started = 0;
    while (getConfig().contains("SiegeSchedule.startCity" + counter))
    {
    	
      log.log(Level.INFO, "getConfig contains SiegeSchedule.Command{0}", Integer.valueOf(counter));
      if ((!getConfig().contains("SiegeSchedule.Command" + counter + ".After")) && (!getConfig().getBoolean("SiegeSchedule.Command" + counter + ".SpecificTime", false)))
      {
        log.log(Level.INFO, "[SiegeScheduler] Command{0} does not have an After value, defaulting to 0.", Integer.valueOf(counter));
        getConfig().set("SiegeSchedule.Command" + counter + ".After", Integer.valueOf(0));
      }
      if (getConfig().getBoolean("SiegeSchedule.Command" + counter + ".SpecificTime", false)) {
        timeTask(counter);
      } else if (getConfig().getBoolean("SiegeSchedule.Command" + counter + ".Repeat"))
      {
        if (!getConfig().contains("SiegeSchedule.Command" + counter + ".Interval")) {
          log.log(Level.INFO, "[SiegeMode] Command{0} has Repeat: true, but Interval is not set! Ignoring this command.", Integer.valueOf(counter));
        } else {
          repeatingTask(counter);
        }
      }
      
    	
      timeTask(counter);
      started++;
      counter++;
    }
    
    counter = 1;
    while (getConfig().contains("SiegeSchedule.endCity" + counter))
    {
    	timeTask(counter);
        started++;
        counter++;
    }
    log.log(Level.INFO, "[SiegeMode] has attempted to put {0} commands on schedule.", Integer.valueOf(started));
  }
  
  public void repeatingTask(final int counter)
  {
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
      public void run()
      {
        SiegeMode.this.runCommand(counter);
      }
    }, getConfig().getInt("SiegeSchedule.Command" + counter + ".After", 0) * 20L, getConfig().getInt("SiegeSchedule.Command" + counter + ".Interval") * 20L);
  }
  
  
  public void nonrepeatingTask(final int counter)
  {
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    {
      public void run()
      {
        SiegeMode.this.runCommand(counter);
      }
    }, getConfig().getInt("SiegeSchedule.Command" + counter + ".After", 0) * 20L);
  }
  */
  
  public void timeTask(final int counter, final String cityType)
  {
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
      public void run()
      {
        SiegeMode.this.runCommand(counter);
      }
    }, getTime(counter, cityType) * 20L, 1728000L);
  }
  
  public void runCommand(int counter)
  {
      int subCounter = 1;
      while (getConfig().contains("SiegeSchedule.startCity" + counter + ".Command" + subCounter)) {
      getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("SiegeSchedule.startCity" + counter + ".Command" + subCounter));
      subCounter++;
      }
      subCounter = 1;
      while (getConfig().contains("SiegeSchedule.endCity" + counter + ".Command" + subCounter)) {
      getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("SiegeSchedule.endCity" + counter + ".Command" + subCounter));
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
      if (!hasPerm(sender, "SiegeMode.use"))
      {
        msg(sender, ChatColor.RED + "You don't have permission to use this command", new Object[0]);
        return true;
      }
      if (args[0].equalsIgnoreCase("reload"))
      {
        msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.WHITE + "Reloding...", new Object[0]);
        reloadConfig();
        msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.GREEN + "Reloaded config file!", new Object[0]);
        return true;
      }
    }
    if (!hasPerm(sender, "SiegeMode.use"))
    {
      msg(sender, ChatColor.RED + "You don't have permission to use this command", new Object[0]);
      return true;
    }
    if (args[0].equalsIgnoreCase("help"))
    {
      msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.WHITE + "Commands:", new Object[] { desc.getVersion() });
      msg(sender, ChatColor.WHITE + "* /sm reload", new Object[0]);
      return true;
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

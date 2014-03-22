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
    getConfig().options().copyDefaults(true);
    getConfig().options().header("Configuration explanation below ~ \n\nALL TIMES ARE IN SECONDS!\nInitial delay is the time before the plugin starts starting \nthe commands in the schedule. This is in place so that other\nplugins have the time to start. You could set this to 0, \nbut errors may occur.\nMake sure the Command1, Command2, Command3 etc. are numbered\nin succession. This will ensure they all load.\n\nAlso make sure you enter a command, heh.\n\n1 minute = 60 seconds. 1 hour = 3600 seconds\nHOUR in 24-hour format!\nSpecificTime commands ALWAYS repeat!\n");
    
    saveConfig();
    getConfig();
    PluginDescriptionFile pdfFile = getDescription();
    log.log(Level.INFO, "[{0}] By MrInfro and Gusko - v{1} enabled.", new Object[] { pdfFile.getName(), pdfFile.getVersion() });
    log.log(Level.INFO, "[{0}] Command execution will start in {1} seconds.", new Object[] { pdfFile.getName(), Integer.valueOf(getConfig().getInt("InitialDelay")) });
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
    int counter = 1;int started = 0;
    while (getConfig().contains("CommandSchedule.Command" + counter))
    {
      log.log(Level.INFO, "getConfig contains CommandSchedule.Command{0}", Integer.valueOf(counter));
      if ((!getConfig().contains("CommandSchedule.Command" + counter + ".After")) && (!getConfig().getBoolean("CommandSchedule.Command" + counter + ".SpecificTime", false)))
      {
        log.log(Level.INFO, "[CommandScheduler] Command{0} does not have an After value, defaulting to 0.", Integer.valueOf(counter));
        getConfig().set("CommandSchedule.Command" + counter + ".After", Integer.valueOf(0));
      }
      if (getConfig().getBoolean("CommandSchedule.Command" + counter + ".SpecificTime", false)) {
        timeTask(counter);
      } else if (getConfig().getBoolean("CommandSchedule.Command" + counter + ".Repeat"))
      {
        if (!getConfig().contains("CommandSchedule.Command" + counter + ".Interval")) {
          log.log(Level.INFO, "[SiegeMode] Command{0} has Repeat: true, but Interval is not set! Ignoring this command.", Integer.valueOf(counter));
        } else {
          repeatingTask(counter);
        }
      }
      else {
        nonrepeatingTask(counter);
      }
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
    }, getConfig().getInt("CommandSchedule.Command" + counter + ".After", 0) * 20L, getConfig().getInt("CommandSchedule.Command" + counter + ".Interval") * 20L);
  }
  
  public void nonrepeatingTask(final int counter)
  {
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    {
      public void run()
      {
        SiegeMode.this.runCommand(counter);
      }
    }, getConfig().getInt("CommandSchedule.Command" + counter + ".After", 0) * 20L);
  }
  
  public void timeTask(final int counter)
  {
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
      public void run()
      {
        SiegeMode.this.runCommand(counter);
      }
    }, getOffset(counter) * 20L, 1728000L);
  }
  
  public void runCommand(int counter)
  {
    getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("CommandSchedule.Command" + counter + ".Command"));
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    PluginDescriptionFile desc = getDescription();
    if (cmd.getName().equalsIgnoreCase("csc"))
    {
      if (args.length == 0)
      {
        msg(sender, ChatColor.RED + "[SiegeMode] " + ChatColor.WHITE + "To see a list of commands type: /csc help", new Object[0]);
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
      msg(sender, ChatColor.WHITE + "* /csc reload", new Object[0]);
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
  

  public int getOffset(int counter)
  {
    this.calendar.setTime(this.date);
    
    int time_in_seconds = this.calendar.get(11) * 3600 + this.calendar.get(12) * 60 + this.calendar.get(13);
    int time_wanted = getConfig().getInt("CommandSchedule.Command" + counter + ".Hour", 0) * 3600 + getConfig().getInt("CommandSchedule.Command" + counter + ".Minute", 0) * 60 + getConfig().getInt("CommandSchedule.Command" + counter + ".Second", 0);
    int Offset;
    if (time_wanted >= time_in_seconds) {
      Offset = time_wanted - time_in_seconds;
    } else {
      Offset = 86400 + time_wanted - time_in_seconds;
    }
    return Offset;
  }
}

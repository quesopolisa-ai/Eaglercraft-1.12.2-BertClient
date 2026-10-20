package baseclient.gg.dhyeybg.baseclient.module;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.module.combat.*;
import baseclient.gg.dhyeybg.baseclient.module.misc.ChestLooter;
import baseclient.gg.dhyeybg.baseclient.module.misc.AutoGG;
import baseclient.gg.dhyeybg.baseclient.module.misc.Scaffold;
import baseclient.gg.dhyeybg.baseclient.module.misc.SellCactus;
import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoBuilder;
import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoMine;
import baseclient.gg.dhyeybg.baseclient.module.movement.*;
import baseclient.gg.dhyeybg.baseclient.module.player.*;
import baseclient.gg.dhyeybg.baseclient.module.render.*;
import baseclient.gg.dhyeybg.baseclient.module.utilities.*;

public class ModuleManager {

	public ArrayList<Module> modules;

	public ModuleManager() {
		modules = new ArrayList<Module>();
		this.modules.add(new ClickGUI());
		this.modules.add(new HUD());
		this.modules.add(new FullBright());
		this.modules.add(new ESP());
		this.modules.add(new Criticals());
		this.modules.add(new KillAura());
		this.modules.add(new AutoCrystal());
		this.modules.add(new AutoWeapon());
		this.modules.add(new AutoGG());
		this.modules.add(new WTap());
		this.modules.add(new HitSounds());
		this.modules.add(new ComboCounter());
		this.modules.add(new BowSilentAim());
		this.modules.add(new BowRelease());
		this.modules.add(new ElytraFly());
		this.modules.add(new ElytraBoost());
		this.modules.add(new BoatFly());
		this.modules.add(new Surround());
		this.modules.add(new Scaffold());
		this.modules.add(new AutoBuilder());
		this.modules.add(new AutoMine());
		this.modules.add(new ChestLooter());
		this.modules.add(new SellCactus());
		this.modules.add(new Sprint());
		this.modules.add(new Velocity());
		this.modules.add(new Reach());
		this.modules.add(new AimAssist());
		this.modules.add(new AutoClicker());
		this.modules.add(new TriggerBot());
		this.modules.add(new NoSlow());
		this.modules.add(new SafeWalk());
		this.modules.add(new InventoryMove());
		this.modules.add(new NoFall());
		this.modules.add(new Fly());
		this.modules.add(new Speed());
		this.modules.add(new HighJump());
		this.modules.add(new LongJump());
		this.modules.add(new AutoWalk());
		this.modules.add(new Parkour());
		this.modules.add(new FastLadder());
		this.modules.add(new NoWeb());
		this.modules.add(new AntiVoid());
		this.modules.add(new Step());
		this.modules.add(new Jesus());
		this.modules.add(new Spider());
		this.modules.add(new AutoTool());
		this.modules.add(new AutoTotem());
		this.modules.add(new AutoArmor());
		this.modules.add(new InventoryManager());
		this.modules.add(new AutoSort());
		this.modules.add(new AutoDrop());
		this.modules.add(new AutoFarm());
		this.modules.add(new AutoReplant());
		this.modules.add(new AutoCraft());
		this.modules.add(new AutoSmelt());
		this.modules.add(new AutoReconnect());
		this.modules.add(new AutoScreenshot());
		this.modules.add(new AutoRespawn());
		this.modules.add(new MiddleClickFriend());
		this.modules.add(new LongPickupRange());
		this.modules.add(new AutoEat());
		this.modules.add(new HotbarReplenish());
		this.modules.add(new AutoFish());
		this.modules.add(new AutoLoginModule());
		this.modules.add(new AutoConfig());
		this.modules.add(new PacketMine());
		this.modules.add(new FastPlace());
		this.modules.add(new Timer());
		this.modules.add(new XRay());
		this.modules.add(new Freecam());
		this.modules.add(new Waypoints());
		this.modules.add(new TimeChanger());
		this.modules.add(new WeatherChanger());
		this.modules.add(new NoRender());
		this.modules.add(new NoParticles());
		this.modules.add(new NoCluster());
		this.modules.add(new TotemCounter());
		this.modules.add(new ChatTimestamps());
		this.modules.add(new Breadcrumbs());
		this.modules.add(new CameraEffects());
	}

	public Module getModule(String name) {
		for (Module m : this.modules) {
			if (m.getName().equalsIgnoreCase(name)) {
				return m;
			}
		}
		return null;
	}

	public ArrayList<Module> getModuleList() {
		return this.modules;
	}

	public ArrayList<Module> getModulesInCategory(Category c) {
		ArrayList<Module> mods = new ArrayList<Module>();
		for (Module m : this.modules) {
			if (m.getCategory() == c) {
				mods.add(m);
			}
		}
		return mods;
	}
}

package dev.tityguard.check;

import dev.tityguard.TityGuard;

public class CheckManager {

    public final SpeedCheck speed;
    public final FlyCheck fly;
    public final NoFallCheck noFall;
    public final TimerCheck timer;
    public final ReachCheck reach;
    public final KillAuraCheck killAura;
    public final AutoClickerCheck autoClicker;
    public final ScaffoldCheck scaffold;
    public final FastBreakCheck fastBreak;
    public final VelocityCheck velocity;

    public CheckManager(TityGuard plugin) {
        speed = new SpeedCheck(plugin);
        fly = new FlyCheck(plugin);
        noFall = new NoFallCheck(plugin);
        timer = new TimerCheck(plugin);
        reach = new ReachCheck(plugin);
        killAura = new KillAuraCheck(plugin);
        autoClicker = new AutoClickerCheck(plugin);
        scaffold = new ScaffoldCheck(plugin);
        fastBreak = new FastBreakCheck(plugin);
        velocity = new VelocityCheck(plugin);
    }
}

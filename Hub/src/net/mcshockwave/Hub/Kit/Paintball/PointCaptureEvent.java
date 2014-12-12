package net.mcshockwave.Hub.Kit.Paintball;

import net.mcshockwave.Hub.Kit.Paintball.PBObjective.ControlPoint;

import org.bukkit.DyeColor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PointCaptureEvent extends Event {

	public ControlPoint	point;
	public DyeColor		team;

	public PointCaptureEvent(ControlPoint point, DyeColor team) {
		this.point = point;
		this.team = team;
	}

	public static final HandlerList	handlers	= new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}

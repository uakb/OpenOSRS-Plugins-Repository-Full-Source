/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 *
 * Modified by farhan1666
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.dropparty;

import com.google.common.base.Strings;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class DropPartyOverlay extends Overlay
{
	private static final int OUTLINE_START_ALPHA = 255;

	private final Client client;
	private final DropPartyConfig config;
	private final DropPartyPlugin plugin;

	@Provides
	DropPartyConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DropPartyConfig.class);
	}

	@Inject
	public DropPartyOverlay(final Client client, final DropPartyPlugin plugin, final DropPartyConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int tiles = config.showAmount();
		if (tiles == 0)
		{
			return null;
		}
		final List<WorldPoint> path = plugin.getPlayerPath();

		final List<WorldPoint> markedTiles = new ArrayList<>();

		for (int i = 0; i < path.size(); i++)
		{
			if (i > plugin.getMAXPATHSIZE() || i > (config.showAmount() - 1))
			{
				break;
			}
			if (path.get(i) != null)
			{
				final LocalPoint local = LocalPoint.fromWorld(client, path.get(i));
				Polygon tilePoly = null;
				if (local != null)
				{
					tilePoly = Perspective.getCanvasTileAreaPoly(client, local, 1);
				}

				if (tilePoly != null)
				{
					if (!markedTiles.contains(path.get(i)))
					{
						graphics.setColor(new Color(setAlphaComponent(config.overlayColor().getRGB(), OUTLINE_START_ALPHA), true));
						graphics.drawPolygon(tilePoly);
						renderTextLocation(graphics, Integer.toString(i + 1), config.textSize(),
							config.fontStyle().getFont(), Color.WHITE, centerPoint(tilePoly.getBounds()), true, 0);
					}
					markedTiles.add(path.get(i));
				}
			}
		}
		return null;
	}

	private Point centerPoint(Rectangle rect)
	{
		int x = (int) (rect.getX() + rect.getWidth() / 2);
		int y = (int) (rect.getY() + rect.getHeight() / 2);
		return new Point(x, y);
	}

	/**
	 * Modifies the alpha component on a Color
	 *
	 * @param color The color to set the alpha value on
	 * @param alpha The alpha value to set on the color
	 * @return color
	 */
	public static int setAlphaComponent(Color color, int alpha)
	{
		return setAlphaComponent(color.getRGB(), alpha);
	}

	/**
	 * Modifies the alpha component on a Color
	 *
	 * @param color The color to set the alpha value on
	 * @param alpha The alpha value to set on the color
	 * @return color
	 */
	public static int setAlphaComponent(int color, int alpha)
	{
		if (alpha < 0 || alpha > 255)
		{
			throw new IllegalArgumentException("alpha must be between 0 and 255.");
		}
		return (color & 0x00ffffff) | (alpha << 24);
	}

	public static void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows, int yOffset)
	{
		graphics.setFont(new Font("Arial", fontStyle, fontSize));
		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(
				canvasPoint.getX(),
				canvasPoint.getY() + yOffset);
			final Point canvasCenterPoint_shadow = new Point(
				canvasPoint.getX() + 1,
				canvasPoint.getY() + 1 + yOffset);
			if (shadows)
			{
				renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
			}
			renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
		}
	}

	public static void renderTextLocation(Graphics2D graphics, Point txtLoc, String text, Color color)
	{
		if (Strings.isNullOrEmpty(text))
		{
			return;
		}

		int x = txtLoc.getX();
		int y = txtLoc.getY();

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);

		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}
package extracells.gui.widget.fluid;

import appeng.api.storage.data.IAEFluidStack;
import extracells.integration.Integration;
import extracells.util.ECConfigHandler;
import extracells.util.GasUtil;
import mekanism.api.gas.Gas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class WidgetFluidSelector extends AbstractFluidWidget {
	private long amount = 0;
	private int color;
	private int borderThickness;

	public WidgetFluidSelector(IFluidSelectorGui guiFluidTerminal, IAEFluidStack stack) {
		super(guiFluidTerminal, 18, 18, stack.getFluidStack().getFluid());
		this.amount = stack.getStackSize();
		this.color = 0xFF00FFFF;
		this.borderThickness = 1;
	}

	private void drawHollowRectWithCorners(int posX, int posY, int height,
		int width, int color, int thickness) {
		drawRect(posX, posY, posX + height, posY + thickness, color);
		drawRect(posX, posY + width - thickness, posX + height, posY + width,
			color);
		drawRect(posX, posY, posX + thickness, posY + width, color);
		drawRect(posX + height - thickness, posY, posX + height, posY + width,
			color);
		drawRect(posX, posY, posX + thickness + 1, posY + thickness + 1, color);
		drawRect(posX + height, posY + width, posX + height - thickness - 1,
			posY + width - thickness - 1, color);
		drawRect(posX + height, posY, posX + height - thickness - 1, posY
			+ thickness + 1, color);
		drawRect(posX, posY + width, posX + thickness + 1, posY + width
			- thickness - 1, color);
	}

	@Override
	public boolean drawTooltip(int posX, int posY, int mouseX, int mouseY) {
		if (this.fluid == null
			|| this.amount <= 0
			|| !isPointInRegion(posX, posY, this.height, this.width,
			mouseX, mouseY)) {
			return false;
		}
		String amountToText = Long.toString(this.amount) + "mB";
		if (ECConfigHandler.shortenedBuckets) {
			if (this.amount > 1000000000L) {
				amountToText = Long.toString(this.amount / 1000000000L)
					+ "MegaB";
			} else if (this.amount > 1000000L) {
				amountToText = Long.toString(this.amount / 1000000L) + "KiloB";
			} else if (this.amount > 9999L) {
				amountToText = Long.toString(this.amount / 1000L) + "B";
			}
		}
		List<String> description = new ArrayList<String>();
		description.add(this.fluid.getLocalizedName(new FluidStack(this.fluid, 0)));
		description.add(amountToText);
		drawHoveringText(description, mouseX - this.guiFluidTerminal.guiLeft(),
			mouseY - this.guiFluidTerminal.guiTop() + 18, Minecraft.getMinecraft().fontRenderer);
		return true;
	}

	@Override
	public void drawWidget(int posX, int posY) {
		Minecraft.getMinecraft().renderEngine
			.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		IAEFluidStack terminalFluid = ((IFluidSelectorGui) this.guiFluidTerminal)
			.getCurrentFluid();
		Fluid currentFluid = terminalFluid != null ? terminalFluid.getFluid()
			: null;
		if (this.fluid != null) {
			TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
			if(Integration.Mods.MEKANISMGAS.isEnabled() && GasUtil.isGas(fluid)) {
				Gas gas = GasUtil.getGas(fluid);
				ResourceLocation gasStill = gas.getIcon();
				TextureAtlasSprite gasStillSprite = null;
				if (gasStill != null) {
					gasStillSprite = textureMap.getTextureExtry(gasStill.toString());
				}
				if (gasStillSprite == null) {
					gasStillSprite = textureMap.getAtlasSprite(fluid.getStill().toString());
				}
				int tint = gas.getTint();
				float r = (tint >> 16 & 0xFF) / 255.0F;
				float g = (tint >> 8 & 0xFF) / 255.0F;
				float b = (tint & 0xFF) / 255.0F;
				GlStateManager.color(r, g, b);
				drawTexturedModalRect(posX + 1, posY + 1, gasStillSprite, this.height - 2, this.width - 2);
				GlStateManager.color(1.0F, 1.0F, 1.0F);
			} else {
				TextureAtlasSprite sprite = textureMap.getAtlasSprite(fluid.getStill().toString());
				GlStateManager.color(1.0F, 1.0F, 1.0F);
				drawTexturedModalRect(posX + 1, posY + 1, sprite, this.height - 2, this.width - 2);
			}
		}
		if (this.fluid == currentFluid) {
			GL11.glColor3f(1, 1, 1);
			drawHollowRectWithCorners(posX, posY, this.height, this.width, this.color, this.borderThickness);
		}
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public long getAmount() {
		return this.amount;
	}

	@Override
	public void mouseClicked(int posX, int posY, int mouseX, int mouseY) {
		if (this.fluid != null
			&& isPointInRegion(posX, posY, this.height, this.width, mouseX,
			mouseY)) {
			((IFluidSelectorGui) this.guiFluidTerminal).getContainer().setSelectedFluid(this.fluid);
		}
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}
}

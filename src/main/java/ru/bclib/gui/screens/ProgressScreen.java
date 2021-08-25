package ru.bclib.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProgressListener;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.gui.gridlayout.GridColumn;
import ru.bclib.gui.gridlayout.GridCustomRenderCell;
import ru.bclib.gui.gridlayout.GridLayout;
import ru.bclib.gui.gridlayout.GridLayout.Alignment;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;
import ru.bclib.gui.gridlayout.GridLayout.VerticalAlignment;
import ru.bclib.gui.gridlayout.GridMessageCell;
import ru.bclib.gui.gridlayout.GridRow;
import ru.bclib.gui.gridlayout.GridScreen;
import ru.bclib.gui.gridlayout.GridStringCell;
import ru.bclib.gui.gridlayout.GridTransform;

class ProgressLogoRender extends GridCustomRenderCell {
	public static final int SIZE = 64;
	public static final int LOGO_SIZE = 512;
	public static final int PIXELATED_SIZE = 512;
	float percentage = 0;
	double time = 0;
	protected ProgressLogoRender() {
		super(SIZE, GridValueType.CONSTANT, SIZE);
	}
	
	@Override
	public void onRender(PoseStack poseStack, GridTransform transform, Object context) {
		time += 0.03;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
		
		final float fScale = (float)(0.3*((Math.sin(time)+1.0)*0.5) + 0.7);
		int height = (int)(transform.height*fScale);
		int width = (int)(transform.width*fScale);
		width -= ((transform.width-width)%2);
		height -= ((transform.height-height)%2);
		final int yOffset = (transform.height-height)/2;
		final int xOffset = (transform.width-width)/2;
		
		final int yBarLocal = (int)(transform.height*percentage);
		final int yBar = transform.top + yBarLocal;
		
		final float relativeY = ((float)(yBarLocal - yOffset)/height);
		
		final int uvTopLogo = (int)(relativeY * LOGO_SIZE);
		final int uvTopPixelated = (int)(relativeY * PIXELATED_SIZE);
		
		if (uvTopLogo>0) {
			RenderSystem.setShaderTexture(0, BCLibScreen.BCLIB_LOGO_LOCATION);
			GuiComponent.blit(poseStack,
				xOffset + transform.left,
				yOffset + transform.top,
				width,
				yBarLocal - yOffset,
				0, 0, LOGO_SIZE, uvTopLogo,
				LOGO_SIZE, LOGO_SIZE
			);
		}
		
		if (uvTopPixelated<PIXELATED_SIZE) {
			RenderSystem.setShaderTexture(0, ProgressScreen.BCLIB_LOGO_PIXELATED_LOCATION);
			GuiComponent.blit(poseStack,
				xOffset + transform.left,
				yBar,
				width,
				height-(yBarLocal - yOffset),
				0, uvTopPixelated, PIXELATED_SIZE, PIXELATED_SIZE-uvTopPixelated,
				PIXELATED_SIZE, PIXELATED_SIZE
			);
		}
		
		if (percentage>0 && percentage<1.0){
			GuiComponent.fill(poseStack,
				transform.left,
				yBar,
				transform.left+transform.width,
				yBar+1,
				0x3FFFFFFF
			);
		}
	}
}

public class ProgressScreen extends GridScreen implements ProgressListener {
	static final ResourceLocation BCLIB_LOGO_PIXELATED_LOCATION = new ResourceLocation(BCLib.MOD_ID, "iconpixelated.png");
	public ProgressScreen(@Nullable Screen parent, Component title, Component description) {
		super(parent, title, 20, false);
		this.description = description;
	}
	
	Component description;
	private Component stageComponent;
	private GridMessageCell stage;
	private GridStringCell progress;
	private ProgressLogoRender progressImage;
	private int currentProgress = 0;
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	public Component getProgressComponent(){
		return getProgressComponent(currentProgress);
	}
	
	private Component getProgressComponent(int pg){
		return new TranslatableComponent("title.bclib.progress").append(": " + pg + "%");
	}
	@Override
	protected void initLayout() {
		grid.addSpacerRow();
		
		GridRow row = grid.addRow(VerticalAlignment.CENTER);
		row.addFiller();
		progressImage = new ProgressLogoRender();
		progressImage.percentage = currentProgress / 100.0f;
		row.addCustomRender(progressImage);
		row.addSpacer();
		
		int textWidth = Math.max(getWidth(description), getWidth(getProgressComponent(100)));
		GridColumn textCol = row.addColumn(0, GridValueType.INHERIT);
		textCol.addRow().addString(description, this);
		textCol.addSpacerRow();
		progress = textCol.addRow().addString(getProgressComponent(), GridLayout.COLOR_GRAY, Alignment.LEFT, this);
		
		row.addFiller();
		
		grid.addSpacerRow(20);
		row = grid.addRow();
		stage = row.addMessage(stageComponent!=null?stageComponent:new TextComponent(""), font, Alignment.CENTER);
	}
	
	@Override
	public void progressStartNoAbort(Component component) {
		this.progressStage(component);
	}
	
	@Override
	public void progressStart(Component component) {
		this.progressStage(component);
		this.progressStagePercentage(0);
	}
	
	@Override
	public void progressStage(Component component) {
		stageComponent = component;
		if (stage!=null) stage.setText(component);
	}
	
	@Override
	public void progressStagePercentage(int i) {
		if (i!=currentProgress) {
			currentProgress = i;
			if (progressImage!=null) progressImage.percentage = currentProgress / 100.0f;
			BCLib.LOGGER.info("  -> progress: " + i + "%");
			if (progress!=null) progress.setText(getProgressComponent());
		}
	}
	
	@Override
	public void stop() {
	
	}
	
	double time = 0;
	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		//time += 0.05;
		//progressStagePercentage(((int)time)%100);
		//progressStagePercentage(1);
		super.render(poseStack, i, j, f);
	}
}
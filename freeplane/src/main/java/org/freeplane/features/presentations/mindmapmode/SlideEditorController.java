package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.core.ui.textchanger.TranslatedElementFactory.createButtonWithIcon;
import static org.freeplane.core.ui.textchanger.TranslatedElementFactory.createCheckBox;
import static org.freeplane.core.ui.textchanger.TranslatedElementFactory.createToggleButtonWithIcon;
import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.PLAYING_STATE_CHANGED;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.features.filter.FilterComposerDialog;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class SlideEditorController{
	
	private Slide slide;
	
	private final JButton btnSelectNodes;
	private final JButton btnSetSelectedNodes;
	private final JButton btnAddSelectedNodes;
	private final JButton btnRemoveSelectedNodes;
	private final JToggleButton tglBtnCenterSelectedNode;
	private final JToggleButton tglbtnChangeZoom;
	private final JLabel lblZoomFactor;
	private final JCheckBox checkBoxShowOnlySelectedNodes;
	private final JCheckBox checkBoxShowAncestors;
	private final JCheckBox checkBoxShowDescendants;
	private final JToggleButton tglbtnSetFilter;
	private final JComponent filterConditionComponentBox;
	private final JToggleButton tglbtnSetFoldingState;
	
	private final JComponent[] allButtons;
	private final JComponent[] filterRelatedButtons;

	private final SlideChangeListener slideChangeListener;

	private final JComponent filterNotSetLabel;

	private final PresentationState presentationState;

	
	@SuppressWarnings("serial")
	public SlideEditorController(PresentationState presentationState) {
		this.presentationState = presentationState;
		presentationState.addPresentationStateListener(new PresentationStateChangeListener() {
			
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				if(presentationStateChangeEvent.eventType == PLAYING_STATE_CHANGED)
					updateUI();
			}
		});
		btnSetSelectedNodes = createSetSelectedNodeButton();
		btnAddSelectedNodes = createAddSelectedNodeButton();
		btnRemoveSelectedNodes = createRemoveSelectedNodeButton();
		btnSelectNodes = createSelectNodesButton();
		tglBtnCenterSelectedNode = createCentersSelectedNodeToggleButton();
		tglbtnChangeZoom = createSetZoomToggleButton();
		lblZoomFactor = new JLabel("100 %");
		lblZoomFactor.setPreferredSize(lblZoomFactor.getPreferredSize());
		checkBoxShowOnlySelectedNodes = createOnlySelectedNodesCheckBox();
		checkBoxShowAncestors = createShowAncestorsCheckBox();
		checkBoxShowDescendants = createShowDescendantsCheckBox();
		tglbtnSetFilter = createSetFilterToggleButton();
		final int minimumHeight = (int) (60 * UITools.FONT_SCALE_FACTOR);
		filterConditionComponentBox = new Box(BoxLayout.X_AXIS){

			@Override
			public Dimension getPreferredSize() {
				final Dimension preferredSize = super.getPreferredSize();
				final Dimension minimumSize = getMinimumSize();
				return new Dimension(Math.max(minimumSize.width, preferredSize.width), Math.max(preferredSize.height, minimumSize.height));
			}
			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
			
		};
		filterConditionComponentBox.setMinimumSize(new Dimension(1, minimumHeight));
		
		tglbtnSetFoldingState = createSetFoldingStateToggleButton();
		
		allButtons = new JComponent[] { btnSelectNodes, btnSetSelectedNodes, btnAddSelectedNodes,
		        btnRemoveSelectedNodes, tglBtnCenterSelectedNode,
		        tglbtnChangeZoom, lblZoomFactor, 
		        checkBoxShowOnlySelectedNodes, checkBoxShowAncestors, checkBoxShowDescendants, tglbtnSetFilter, 
		        tglbtnSetFoldingState};
		filterRelatedButtons = new JComponent[]{checkBoxShowAncestors, checkBoxShowDescendants};
		slideChangeListener = new SlideChangeListener() {
			
			@Override
			public void onSlideModelChange(SlideChangeEvent changeEvent) {
				updateUI();
			}
		};
		filterNotSetLabel = TranslatedElementFactory.createLabel("slide.nofilter");
		filterNotSetLabel.setEnabled(false);
		filterNotSetLabel.setForeground(Color.DARK_GRAY);
		disableUI();
	}

	private JButton createSetSelectedNodeButton() {
		JButton btnSetSelectedNode = createButtonWithIcon("SetFilterSlideContent.icon", "slide.set.tooltip");
		btnSetSelectedNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSetSelectedNode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection().getOrderedSelectionIds();
				UndoableSlide.of(slide).setSelectedNodeIds(new LinkedHashSet<>(selection));
			}
		});

		return btnSetSelectedNode;
	}

	private JButton createAddSelectedNodeButton() {
		JButton btnSetSelectedNode = createButtonWithIcon("AddSlideContent.icon", "slide.add.tooltip");
		btnSetSelectedNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSetSelectedNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection()
				    .getOrderedSelectionIds();
				UndoableSlide.of(slide).addSelectedNodeIds(selection);
			}
		});
		return btnSetSelectedNode;
	}

	private JButton createRemoveSelectedNodeButton() {
		JButton btnSetSelectedNode = createButtonWithIcon("RemoveSlideContent.icon", "slide.remove.tooltip");
		btnSetSelectedNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSetSelectedNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection()
				    .getOrderedSelectionIds();
				UndoableSlide.of(slide).removeSelectedNodeIds(selection);
			}
		});
		return btnSetSelectedNode;
	}

	private JToggleButton createSetZoomToggleButton() {
		final JToggleButton btnSetsZoom = createToggleButtonWithIcon("SetZoomSlideContent.icon", "slide.setzoom");
		btnSetsZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean changesZoom = ! slide.changesZoom();
				UndoableSlide.of(slide).setChangesZoom(changesZoom);
				if(changesZoom) {
					UndoableSlide.of(slide).setZoom(Controller.getCurrentController().getMapViewManager().getZoom());
				}
			}
		});
		return btnSetsZoom;
	}
	private JToggleButton createCentersSelectedNodeToggleButton() {
		final JToggleButton checkBoxOnlySpecificNodes = createToggleButtonWithIcon("CenterSelectedNodeSlideContent.icon", "slide.centernode");
		checkBoxOnlySpecificNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxOnlySpecificNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String centeredNodeId = slide.getCenteredNodeId();
				final IMapSelection selection = Controller.getCurrentController().getSelection();
				if(centeredNodeId == null) {
					final NodeModel selected = selection.getSelected();
					if(selected != null)
						UndoableSlide.of(slide).setCenteredNodeId(selected.getID());
					else
						UndoableSlide.of(slide).setCenteredNodeId(null);
				} else {
					UndoableSlide.of(slide).setCenteredNodeId(null);
					final MapModel map = Controller.getCurrentController().getMap();
					final NodeModel node = map.getNodeForID(centeredNodeId);
					if(node != null)
						selection.selectAsTheOnlyOneSelected(node);
				}
				checkBoxOnlySpecificNodes.setSelected(slide.getCenteredNodeId() != null);
			}
		});
		return checkBoxOnlySpecificNodes;
	}

	private JCheckBox createOnlySelectedNodesCheckBox() {
		final JCheckBox checkBoxOnlySpecificNodes = createCheckBox("slide.showonlyselected");
		checkBoxOnlySpecificNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxOnlySpecificNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableSlide.of(slide).setShowsOnlySpecificNodes(! slide.showsOnlySpecificNodes());
			}
		});
		return checkBoxOnlySpecificNodes;
	}

	private JCheckBox createShowAncestorsCheckBox() {
				
		final JCheckBox checkBoxShowAncestors = createCheckBox("slide.showancestors");
		checkBoxShowAncestors.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxShowAncestors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableSlide.of(slide).setShowsAncestors(! slide.showsAncestors());
			}
		});
		return checkBoxShowAncestors;
	}

	private JCheckBox createShowDescendantsCheckBox() {
		final JCheckBox checkBoxShowDescendants = createCheckBox("slide.descendants");
		checkBoxShowDescendants.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxShowDescendants.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableSlide.of(slide).setShowsDescendants(! slide.showsDescendants());
			}
		});
		return checkBoxShowDescendants;
	}


	
	private FilterComposerDialog filterComposerDialog = null;
	private JToggleButton createSetFilterToggleButton() {
		final JToggleButton tglbtnSetFilter = createToggleButtonWithIcon("SetFilterSlideContent.icon", "slide.setfilter");
		tglbtnSetFilter.setAlignmentX(Component.CENTER_ALIGNMENT);
		tglbtnSetFilter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if( filterComposerDialog == null)
					filterComposerDialog = new FilterComposerDialog();
				ASelectableCondition oldCondition = slide.getFilterCondition();
				if(oldCondition != null) {
					filterComposerDialog.addCondition(oldCondition);
					filterComposerDialog.setSelectedItem(oldCondition);
					UndoableSlide.of(slide).setFilterCondition(null);
				} else {
					filterComposerDialog.show();
					List<ASelectableCondition> conditions = filterComposerDialog.getConditions();
					if(filterComposerDialog.isSuccess()) {
						ASelectableCondition newCondition = conditions.isEmpty() ? null : conditions.get(0);
						UndoableSlide.of(slide).setFilterCondition(newCondition);
					}
				}
			    tglbtnSetFilter.setSelected(slide.getFilterCondition() != null);
			    
			}
		});
		return tglbtnSetFilter;
	}

	private JButton createSelectNodesButton() {
		JButton btnHighlightSlideContent = createButtonWithIcon("SelectSlideContent.icon", "slide.select.tooltip");
		btnHighlightSlideContent.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnHighlightSlideContent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				slide.replaceCurrentSelection();
			}
		});
		return btnHighlightSlideContent;
	}
	private JToggleButton createSetFoldingStateToggleButton() {
		JToggleButton tglbtnSetFilter = createToggleButtonWithIcon("SetFoldingSlideContent.icon", "slide.setfoldingstate");
		tglbtnSetFilter.setAlignmentX(Component.CENTER_ALIGNMENT);
		tglbtnSetFilter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(! slide.foldsNodes())
					UndoableSlide.of(slide).setCurrentFoldedNodeIDs();
				else
					UndoableSlide.of(slide).unsetFoldsNodes();
			}
		});
		return tglbtnSetFilter;
	}


	Box createSlideContentBox() {
		
		Box content = Box.createVerticalBox();
		content.setName("!!!!");
		
		Box selectionBox = Box.createHorizontalBox();
		selectionBox.add(btnSetSelectedNodes);
		selectionBox.add(btnAddSelectedNodes);
		selectionBox.add(btnRemoveSelectedNodes);
		selectionBox.add(btnSelectNodes);
		content.add(selectionBox);
		content.add(tglBtnCenterSelectedNode);
		Box zoomBox = Box.createHorizontalBox();
		zoomBox.add(tglbtnChangeZoom);
		zoomBox.add(lblZoomFactor);
		content.add(zoomBox);
		content.add(checkBoxShowOnlySelectedNodes);
		content.add(checkBoxShowAncestors);
		content.add(checkBoxShowDescendants);
		content.add(tglbtnSetFilter);
		content.add(filterConditionComponentBox);
		filterConditionComponentBox.setAlignmentX(Box.CENTER_ALIGNMENT);
		TranslatedElementFactory.createTitledBorder(filterConditionComponentBox, "slide.filter");
		
		content.add(tglbtnSetFoldingState);

		Box contentWithMargins = Box.createHorizontalBox();
		TranslatedElementFactory.createTitledBorder(contentWithMargins, "slide.content");
		contentWithMargins.add(Box.createHorizontalGlue());
		contentWithMargins.add(content);
		contentWithMargins.add(Box.createHorizontalGlue());
		return contentWithMargins;
	}


	public void setSlide(Slide newSlide) {
		if(slide != null)
			slide.removeSlideChangeListener(slideChangeListener);
		this.slide = newSlide;
		updateUI();
		if(newSlide != null){
			slide.addSlideChangeListener(slideChangeListener);
		}
	}


	private void disableUI() {
		for(JComponent c : allButtons)
			c.setEnabled(false);
		updateFilter();
	}


	private void updateUI() {
		if (slide == null) {
			disableUI();
			checkBoxShowOnlySelectedNodes.setSelected(false);
			tglBtnCenterSelectedNode.setSelected(false);
			tglbtnChangeZoom.setSelected(false);
			lblZoomFactor.setText("");
			checkBoxShowOnlySelectedNodes.setSelected(false);
			checkBoxShowAncestors.setSelected(false);
			checkBoxShowDescendants.setSelected(false);
			checkBoxShowAncestors.setSelected(false);
			tglbtnSetFilter.setSelected(false);
		} else {
			for(JComponent c : allButtons)
				c.setEnabled(true);
			final boolean showsOnlySpecificNodes = slide.showsOnlySpecificNodes();
			checkBoxShowOnlySelectedNodes.setSelected(showsOnlySpecificNodes);
			final boolean centersSelectedNode = slide.getCenteredNodeId() != null;
			tglBtnCenterSelectedNode.setSelected(centersSelectedNode);
			final ASelectableCondition filterCondition = slide.getFilterCondition();
			if (! presentationState.isPresentationRunning())
				for(JComponent c : filterRelatedButtons)
					c.setEnabled(showsOnlySpecificNodes || filterCondition != null);
			final boolean changesZoom = slide.changesZoom();
			tglbtnChangeZoom.setSelected(changesZoom);
			lblZoomFactor.setText(changesZoom ? Math.round(slide.getZoom() * 100) + "%" : "");
			checkBoxShowOnlySelectedNodes.setSelected(showsOnlySpecificNodes);
			checkBoxShowAncestors.setSelected(slide.showsAncestors());
			checkBoxShowDescendants.setSelected(slide.showsDescendants());
			checkBoxShowAncestors.setSelected(slide.showsAncestors());
			tglbtnSetFilter.setSelected(filterCondition != null);
			tglbtnSetFoldingState.setSelected(slide.foldsNodes());
		}
		updateFilter();
	}

	private void updateFilter() {
		removeFilterComponent();
		final ASelectableCondition filterCondition = slide != null ? slide.getFilterCondition() : null;
		if(filterCondition != null) {
			final JComponent component = filterCondition.createGraphicComponent();
			filterConditionComponentBox.add(component);
		}
		else{
			filterConditionComponentBox.add(filterNotSetLabel);
		}
		filterConditionComponentBox.revalidate();
	}

	private void removeFilterComponent() {
		while(filterConditionComponentBox.getComponentCount() > 0)
			filterConditionComponentBox.remove(0);
	}
	
	

}
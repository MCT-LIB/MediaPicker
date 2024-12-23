package com.mct.mediapicker.common.dragselect;

public class DragSelectionProcessor implements DragSelectTouchListener.OnAdvancedDragSelectListener {

    /**
     * Different existing selection modes
     */
    public enum Mode {
        /**
         * simply selects each item you go by and unselects on move back
         */
        Simple,
        /**
         * toggles each items original state, reverts to the original state on move back
         */
        ToggleAndUndo,
        /**
         * toggles the first item and applies the same state to each item you go by and applies inverted state on move back
         */
        FirstItemDependent,
        /**
         * toggles the item and applies the same state to each item you go by and reverts to the original state on move back
         */
        FirstItemDependentToggleAndUndo
    }

    private Mode mMode;
    private ISelectionHandler mSelectionHandler;
    private ISelectionStartFinishedListener mStartFinishedListener;
    private boolean mFirstWasSelected;
    private boolean mCheckSelectionState = false;

    /**
     * @param selectionHandler the handler that takes care to handle the selection events
     */
    public DragSelectionProcessor(ISelectionHandler selectionHandler) {
        mMode = Mode.Simple;
        mSelectionHandler = selectionHandler;
        mStartFinishedListener = null;
    }

    /**
     * @param mode the mode in which the selection events should be processed
     * @return this
     */
    public DragSelectionProcessor withMode(Mode mode) {
        mMode = mode;
        return this;
    }

    /**
     * @param selectionHandler the handler that takes care to handle the selection events
     * @return this
     */
    public DragSelectionProcessor withSelectionHandler(ISelectionHandler selectionHandler) {
        mSelectionHandler = selectionHandler;
        return this;
    }

    /**
     * @param startFinishedListener a listener that get notified when the drag selection is started or finished
     * @return this
     */
    public DragSelectionProcessor withStartFinishedListener(ISelectionStartFinishedListener startFinishedListener) {
        mStartFinishedListener = startFinishedListener;
        return this;
    }

    /**
     * If this is enabled, the processor will check if an items selection state is toggled before notifying the {@link ISelectionHandler}
     *
     * @param check true, if this check should be enabled
     * @return this
     */
    public DragSelectionProcessor withCheckSelectionState(boolean check) {
        mCheckSelectionState = check;
        return this;
    }

    @Override
    public void onSelectionStarted(int start) {
        mFirstWasSelected = mSelectionHandler.isSelected(start);

        switch (mMode) {
            case Simple: {
                mSelectionHandler.updateSelection(start, start, true, true);
                break;
            }
            case ToggleAndUndo:
            case FirstItemDependent:
            case FirstItemDependentToggleAndUndo: {
                mSelectionHandler.updateSelection(start, start, !mFirstWasSelected, true);
                break;
            }
        }
        if (mStartFinishedListener != null)
            mStartFinishedListener.onSelectionStarted(start, mFirstWasSelected);
    }

    @Override
    public void onSelectionFinished(int end) {
        if (mStartFinishedListener != null)
            mStartFinishedListener.onSelectionFinished(end);
    }

    @Override
    public void onSelectChange(int start, int end, boolean isSelected) {
        switch (mMode) {
            case Simple: {
                checkedUpdateSelection(start, end, isSelected);
                break;
            }
            case ToggleAndUndo: {
                for (int i = start; i <= end; i++)
                    checkedUpdateSelection(i, i, isSelected != mSelectionHandler.isSelected(i));
                break;
            }
            case FirstItemDependent: {
                checkedUpdateSelection(start, end, !mFirstWasSelected && isSelected);
                break;
            }
            case FirstItemDependentToggleAndUndo: {
                for (int i = start; i <= end; i++)
                    checkedUpdateSelection(i, i, isSelected ? !mFirstWasSelected : mSelectionHandler.isSelected(i));
                break;
            }
        }
    }

    private void checkedUpdateSelection(int start, int end, boolean newSelectionState) {
        if (mCheckSelectionState) {
            for (int i = start; i <= end; i++) {
                if (mSelectionHandler.isSelected(i) != newSelectionState)
                    mSelectionHandler.updateSelection(i, i, newSelectionState, false);
            }
        } else
            mSelectionHandler.updateSelection(start, end, newSelectionState, false);
    }

    public interface ISelectionHandler {
        /**
         * Can be ignored for {@link Mode#Simple}         *
         *
         * @param index the index which selection state wants to be known
         * @return the current selection state of the passed in index
         */
        boolean isSelected(int index);

        /**
         * update your adapter and select select/unselect the passed index range, you be get a single for all modes but {@link Mode#Simple} and {@link Mode#FirstItemDependent}
         *
         * @param start             the first item of the range who's selection state changed
         * @param end               the last item of the range who's selection state changed
         * @param isSelected        true, if the range should be selected, false otherwise
         * @param calledFromOnStart true, if it was called from the {@link DragSelectionProcessor#onSelectionStarted(int)} event
         */
        void updateSelection(int start, int end, boolean isSelected, boolean calledFromOnStart);
    }

    public interface ISelectionStartFinishedListener {
        /**
         * @param start                  the item on which the drag selection was started at
         * @param originalSelectionState the original selection state
         */
        void onSelectionStarted(int start, boolean originalSelectionState);

        /**
         * @param end the item on which the drag selection was finished at
         */
        void onSelectionFinished(int end);
    }
}
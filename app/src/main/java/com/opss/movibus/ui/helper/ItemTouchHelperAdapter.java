package com.opss.movibus.ui.helper;

/**
 * Created by cassio on 07/04/18.
 */

public interface ItemTouchHelperAdapter {

    public boolean onItemMove(int fromPosition, int toPosition);

    public void onItemDismiss(int position);
}

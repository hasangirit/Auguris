package com.example.asus.auguris.Navigation;

import android.support.design.widget.BottomSheetBehavior;

import com.mapbox.services.android.navigation.ui.v5.NavigationContract;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.commons.models.Position;

import android.support.design.widget.BottomSheetBehavior;

import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.commons.models.Position;

class NavigationPresenter {

    public NavigationContract.View view;

    NavigationPresenter(NavigationContract.View view) {
        this.view = view;
    }

    void onMuteClick(boolean isMuted) {
        view.setMuted(isMuted);
    }

    void onRecenterClick() {
        view.setSummaryBehaviorHideable(false);
        view.setSummaryBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
        view.resetCameraPosition();
        view.hideRecenterBtn();
    }

    void onExpandArrowClick(int summaryBehaviorState) {
        view.setSummaryBehaviorState(summaryBehaviorState == BottomSheetBehavior.STATE_COLLAPSED
                ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
    }

    void onCancelBtnClick() {
        view.finishNavigationView();
    }

    void onDirectionsOptionClick() {
        view.setSheetShadowVisibility(false);
        view.setSummaryOptionsVisibility(false);
        view.setSummaryDirectionsVisibility(true);
    }

    void onMapScroll() {
        view.setSummaryBehaviorHideable(true);
        view.setSummaryBehaviorState(BottomSheetBehavior.STATE_HIDDEN);
        view.setCameraTrackingEnabled(false);
    }

    void onSummaryBottomSheetExpanded() {
        view.setCancelBtnClickable(false);

        if (view.isSummaryDirectionsVisible()) {
            view.setSheetShadowVisibility(false);
        }
    }

    void onSummaryBottomSheetCollapsed() {
        view.setCancelBtnClickable(true);
        view.setSummaryOptionsVisibility(true);
        view.setSummaryDirectionsVisibility(false);
    }

    void onSummaryBottomSheetHidden() {
        view.showRecenterBtn();
    }

    void onBottomSheetSlide(float slideOffset, boolean sheetShadowVisible) {
        if (slideOffset < 1f && !sheetShadowVisible) {
            view.setSheetShadowVisibility(true);
        }
        if (view.isSummaryDirectionsVisible()) {
            view.animateInstructionViewAlpha(1 - slideOffset);
        }
        view.animateCancelBtnAlpha(1 - slideOffset);
        view.animateExpandArrowRotation(180 * slideOffset);
    }

    void onRouteUpdate(DirectionsRoute directionsRoute) {
        view.drawRoute(directionsRoute);
    }

    void onDestinationUpdate(Position position) {
        view.addMarker(position);
    }

    void onNavigationRunning() {
        view.showInstructionView();
    }
}

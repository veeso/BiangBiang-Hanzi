//
//  CameraZoomTests.swift
//  BiangBiang Hanzi
//

@testable import BiangBiang_Hanzi
import CoreGraphics
import Testing

struct CameraZoomTests {
    @Test func presetsEmptyWhenMaxBelowOne() {
        #expect(availablePresets(maxUIZoom: 0.8) == [])
    }

    @Test func presetsOnlyOneWhenMaxIsOne() {
        #expect(availablePresets(maxUIZoom: 1.0) == [1.0])
    }

    @Test func presetsOneAndTwoWhenMaxBetweenTwoAndFive() {
        #expect(availablePresets(maxUIZoom: 3.4) == [1.0, 2.0])
    }

    @Test func presetsAllThreeWhenMaxAtLeastFive() {
        #expect(availablePresets(maxUIZoom: 5.0) == [1.0, 2.0, 5.0])
        #expect(availablePresets(maxUIZoom: 10.0) == [1.0, 2.0, 5.0])
    }

    @Test func clampZoomLowerBound() {
        #expect(clampZoom(0.2, min: 1.0, max: 5.0) == 1.0)
    }

    @Test func clampZoomUpperBound() {
        #expect(clampZoom(8.0, min: 1.0, max: 5.0) == 5.0)
    }

    @Test func clampZoomWithinRange() {
        #expect(clampZoom(2.5, min: 1.0, max: 5.0) == 2.5)
    }

    @Test func uiZoomToDeviceZoomMultipliesBySwitchOver() {
        #expect(uiZoomToDeviceZoom(1.0, switchOverFactor: 2.0) == 2.0)
        #expect(uiZoomToDeviceZoom(2.5, switchOverFactor: 2.0) == 5.0)
    }

    @Test func deviceZoomToUIZoomDividesBySwitchOver() {
        #expect(deviceZoomToUIZoom(2.0, switchOverFactor: 2.0) == 1.0)
        #expect(deviceZoomToUIZoom(6.0, switchOverFactor: 2.0) == 3.0)
    }

    @Test func mappingIsIdentityWhenSwitchOverIsOne() {
        #expect(uiZoomToDeviceZoom(3.0, switchOverFactor: 1.0) == 3.0)
        #expect(deviceZoomToUIZoom(3.0, switchOverFactor: 1.0) == 3.0)
    }
}

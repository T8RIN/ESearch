package ru.tech.easysearch.functions

object ScriptsJS {

    const val desktopScript =
        "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=1200px');"

    const val privacyScript =
        "if (navigator.globalPrivacyControl === undefined) { Object.defineProperty(navigator, 'globalPrivacyControl', { value: true, writable: false,configurable: false});} else {try { navigator.globalPrivacyControl = true;} catch (e) { console.error('globalPrivacyControl is not writable: ', e); }};"

    const val doNotTrackScript1 =
        "if (navigator.doNotTrack === null) { Object.defineProperty(navigator, 'doNotTrack', { value: 1, writable: false,configurable: false});} else {try { navigator.doNotTrack = 1;} catch (e) { console.error('doNotTrack is not writable: ', e); }};"

    const val doNotTrackScript2 =
        "if (window.doNotTrack === undefined) { Object.defineProperty(window, 'doNotTrack', { value: 1, writable: false,configurable: false});} else {try { window.doNotTrack = 1;} catch (e) { console.error('doNotTrack is not writable: ', e); }};"

    const val doNotTrackScript3 =
        "if (navigator.msDoNotTrack === undefined) { Object.defineProperty(navigator, 'msDoNotTrack', { value: 1, writable: false,configurable: false});} else {try { navigator.msDoNotTrack = 1;} catch (e) { console.error('msDoNotTrack is not writable: ', e); }};"
}
'use strict';

import React, { AppRegistry, StyleSheet, Text, View } 
  from 'react-native'
import App 
  from './app';

const fessbox_android = React.createClass({
  render() {
    return (
      <App />
    )
  }
})

AppRegistry.registerComponent('fessbox_android', () => fessbox_android)

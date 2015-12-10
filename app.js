import Slider   from 'react-native-slider'
import Button   from 'react-native-button'
import ListView from 'ListView'

import React, { StyleSheet, View, Text, TouchableHighlight, TouchableNativeFeedback, TouchableWithoutFeedback }    
  from 'react-native'
import { Icon } 
  from 'react-native-icons'
import { SegmentedControls } 
  from 'react-native-radio-buttons'

class ModeSelect extends React.Component {
  constructor(props) {
    super(props)
  }
  render() {
    const { icon, label } = this.props
    return (
      <View style={{
        flexDirection    : 'column',
        alignItems       : 'center',
        justifyContent   : 'center'
      }}>
        <Icon
          name  = {`material|${icon}`}
          size  = {32}
          color = '#333333'
          style = {{
            flex         : 1,
            width        : 32,
            height       : 38,
            alignItems   : 'center'
          }}
        />
        <Text>{label}</Text>
      </View>
    )
  }
}

class App extends React.Component {
  constructor(props) {
    super(props)
    const ds = new ListView.DataSource({
      rowHasChanged : (r1, r2) => r1 !== r2
    })
    this.state = {
      dataSource : ds.cloneWithRows(['row 1', 'row 2']),
      mode       : 'ivr'
    }
  }
  setMode(mode) {
    this.setState({mode})
  }
  renderModeContext() {
    return (
      <View style={styles.modeWrapper}>
        <Text>Free</Text>
      </View>
    )
  }
  render() {
    const { dataSource } = this.state
    const modes = ['host', 'master', 'on_hold', 'ivr']
    const meta = {
      host : {
        label : 'Host',
        icon  : 'hearing'
      },
      master : {
        label : 'Master',
        icon  : 'mic'
      },
      on_hold : {
        label : 'On hold',
        icon  : 'pause-circle'
      },
      ivr : {
        label : 'IVR',
        icon  : 'voicemail'
      }
    }
    return (
      <View>
        <ListView
          dataSource = {dataSource}
          renderRow  = {data => (
          <View>
            <View style={styles.title}>
              <Text>Channel name</Text>
            </View>
            {this.renderModeContext()}
            <View style={styles.controls}>
              <Button>
                <View style={styles.muteButton}>
                  <Icon
                    name  = 'material|volume-up'
                    size  = {32}
                    color = '#333333'
                    style = {styles.icon}
                  />
                </View>
              </Button>
              <View style={styles.slider}>
                <Slider />
              </View>
              <View style={styles.modeSelect}>
                <SegmentedControls
                  options         = {modes}
                  onSelection     = {this.setMode.bind(this)}
                  selectedOption  = {this.state.mode}
                  renderOption    = {(option, selected) => {
                    const { icon, label } = meta[option]
                    return (
                      <View style={{
                        padding         : 10,
                        backgroundColor : selected ? '#aaaaaa' : 'transparent'}}>
                        <ModeSelect
                          icon    = {icon}
                          label   = {label}
                        />
                      </View>
                    )
                  }}
                />
              </View>
            </View>
          </View>
        )} />
      </View>
    )
  }
}

const styles = StyleSheet.create({
  modeSelect         : {
    flexDirection    : 'row'
  },
  title              : {
    flexDirection    : 'row',
    alignItems       : 'center',
    justifyContent   : 'center',
    margin           : 20
  },
  controls           : {
    flexDirection    : 'row',
    alignItems       : 'center',
    justifyContent   : 'center',
    margin           : 20
  },
  modeWrapper        : {
    flexDirection    : 'row',
    alignItems       : 'center',
    justifyContent   : 'center',
    borderWidth      : 1,
    borderColor      : '#d6d7da',
    margin           : 20
  },
  slider             : {
    flex             : 1,
    marginHorizontal : 16
  },
  muteButton         : {
    width            : 64,
    height           : 64,
    borderRadius     : 64,
    alignItems       : 'center',
    justifyContent   : 'center',
    borderWidth      : 1,
    borderColor      : '#d6d7da'
  },
  buttonWrapper      : {
    padding          : 10
  },
  icon               : {
    flex             : 1,
    width            : 32,
    height           : 32,
    alignItems       : 'center'
  }
})

export default App

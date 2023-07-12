import MainView from './views/MainView';
import Stylesheet from 'reactjs-stylesheet';

function App() {
  return (
    <div style={styles.app}>
      <header style={styles.appHeader}>
        <MainView />
      </header>
    </div>
  );
}

const styles = Stylesheet.create({
  app: {
    textAlign: 'center',
  },
  appHeader: {
    backgroundColor: '#5c8f99',
    height: '100vh',
    width: '100vw',
    display: 'flex',
    fontSize: '3vh',
    color: 'white',
  }
})

export default App;

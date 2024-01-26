import "./App.css";
import { Reset } from "styled-reset";
import { Fragment } from "react";

import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import Homepage from "./pages/Homepage";
import Navbar from "./components/Navbar";
import SignupModal from "./components/Modals/SignupModal"
import SigninModal from './components/Modals/SigninModal'

function App() {
  return (
    <Fragment>
        <Reset />
      <BrowserRouter>
      <Routes>
        <Route exact  path='/Home' element={<Homepage />}> </Route>
        <Route path='/nav' element={<Navbar />}> </Route>
        <Route path='/user' element={<SignupModal />}></Route> 
        <Route path='/login' element={<SigninModal />}></Route> 
        <Route path='*' Component={Homepage} /> 
        <Route path='/nav2' Component={Navbar} />
        </Routes>
        <Link to='/home'>Homepage(실험중인곳)</Link>
        <Link to='/'>기본 경로</Link>
        <Link to='/nav'>Navbar</Link>
        <Link to='/user'>Signup</Link>
        <Link to='/login'>SignIn</Link>
      </BrowserRouter>
        <div className="App">
          {/* <h1>test</h1> */}
        </div>
    </Fragment>
  );
}

export default App;

# HowDoTheHorsiesMove
Minichess software with different players, designed to connect to minichess server https://github.com/BartMassey/IMCS

more information on minichess and the rules can be found here: http://wiki.cs.pdx.edu/minichess/

currently supports several players including, random, negamax of arbitrary depth (0 depth is a simple heuristic player) with support for alpha-beta pruning, as well as a timed iterativly deepening player that also uses negamax with alpha-beta. most options are set at launch via flags, the available flags are as follow:

  -s <SERVER> <PORT>
  
	set the server and port to connect to for netowrked games. defaulted to current live server.
	
  -u <USERNAME>
  
	sets username to use for login. must be registered on the server before use. default is currently mine during devlopment.
	
  -p <PASSWORD>
  
	password to use for login. you can't have mine, get your own.
	
  -t <PLAYER_TYPE>
  
	sets the type of player to use. this value is used for online games, and is also the type of player 1 in local games.
	available player types are:
		0: time limited, iterative deepening, alpha-beta negamax player (currently hard-coded to 5 minutes provided as default on the server.)
		1: fixed depth, alpha-beta negamax player. depth can be set via -d and -f flags found below, default is 6.
		2: fixed depth, negamax player using only win pruning. depth as above.
		3: random player. selects a random move from all available.
	
  -2 <PLAYER2_TYPE>
  
	sets the game to a local match between to players without connecting to a server. any server related values will be ignored if this is set.
	also accepts the type of player that player 2 will be. see above for selection.
	
  -w
  
	sets player 1 to be the white player and player 2 to be the black player. this is the default.
	
  -b
  
	opposite of above, sets player 1 to black and player 2 to white.
	
  -a <GAME_ID>
  
	accepts the given game ID. player side will be set automatically as needed.
  
  -o
  
	offers a game on the sever. this is the default behavior. desired color can be set by -b and -w flags found above.
	
  -d <WHITE_DEPTH>
  
	sets the desired depth for the white palyer if they are using a fixed depth type player, ignored otherwise.
	note: this is NOT for player 1, but for the white player, whichever that is. if -f is not used, this sets the depth for both players. 
	default is 6, >10 is not advised for any player, and lower is reccomended without alpha-beta.
	
  -f <BLACK_DEPTH>
  
	sets the depth for the black player. if this flag is not used, the value will match that of the white player. see -d for more.
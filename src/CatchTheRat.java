import java.util.Random;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

class Location
{
   int row;
   int col;
   int val;

   public Location()
   {
      this(0,0,0);
   }

   public Location(int row, int col, int val)
   {
      this.row = row;
      this.col = col;
      this.val = val;
   }
}


public class CatchTheRat extends JFrame{

   private static int[][] board; //Game Board
   private static int[][] tempBoard;
   private static int boardSize; //Size of the Board

   private static Random generator = new Random();

   private static JPanel boardPanel;
   private static JPanel boardHolderPanel;
   private static JPanel controlPanel;

   private static JButton undoButton;

   private static Square[][] boardObject;

   private static Location currLocation;

   private static Stack<Location> userMove;
   private static Stack<Location> ratMove;

   private static ArrayList<Location> exits;

   private static int[][] dir = {{-1,0},{-1,1},{0,1},{1,1},
                                       {1,0},{1,-1},{0,-1},{-1,-1}};

   public CatchTheRat(int size)
   {
      board = new int[size+2][size+2]; // Always square board. Add Borders.
      boardSize = size;

      userMove = new Stack<Location>();
      ratMove = new Stack<Location>();
      exits = new ArrayList<Location>();

      currLocation = new Location();
      generateBoard();

      boardPanel = new JPanel();
      boardPanel.setLayout(new GridLayout(board.length, board.length, 0, 0));
      boardObject = new Square[boardSize+2][boardSize+2];
      for (int row = 0;row < board.length ; row++ ) {

         for (int col = 0;col < board.length ; col++ ) {

            boardObject[row][col] = new Square(board[row][col],(board.length*row)+col);
            boardPanel.add(boardObject[row][col]);
         }
      }

      boardHolderPanel = new JPanel();
      boardHolderPanel.add(boardPanel, BorderLayout.CENTER);
      add(boardHolderPanel, BorderLayout.CENTER);

      undoButton = new JButton("UNDO");

      undoButton.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  undoMove();
               }
            }
            );

      controlPanel = new JPanel();

      controlPanel.add(undoButton, BorderLayout.CENTER);

      add(controlPanel, BorderLayout.SOUTH);

      setVisible(true);

   }

   private static void generateBoard()
   {
      //Make the borders as walls
      for(int i = 0;i < boardSize+2; i++)
      {
         board[0][i] = 1;
         board[boardSize+1][i] = 1;
         board[i][0] = 1;
         board[i][boardSize+1] = 1;
      }

      //make exit points on row 1 and boardSize

      for(int i = 0;i < boardSize/2 ; i++)
      {
         int col = generator.nextInt(boardSize)+1;
         if(board[1][col]!=0)
            i--;
         else
         {
            exits.add(new Location(1,col,-1));
            board[1][col] = -1;
         }
      }

      for(int i = 0;i < boardSize/2 ; i++)
      {
         int col = generator.nextInt(boardSize)+1;
         if(board[boardSize][col]!=0)
            i--;
         else
         {
            exits.add(new Location(boardSize,col,-1));
            board[boardSize][col] = -1;
         }
      }


      //make exit points on col 1 and boardSize

      for(int i = 0;i < boardSize/2 ; i++)
      {
         int row = generator.nextInt(boardSize)+1;
         if(board[row][1]!=0)
            i--;
         else
         {
            exits.add(new Location(row,1,-1));
            board[row][1] = -1;
         }
      }

      for(int i = 0;i < boardSize/2 ; i++)
      {
         int row = generator.nextInt(boardSize)+1;
         if(board[row][boardSize]!=0)
            i--;
         else
         {
            exits.add(new Location(row,boardSize,-1));
            board[row][boardSize] = -1;
         }
      }


      //make walls

      for(int i = 0;i < boardSize; i++)
      {
         int row = generator.nextInt(boardSize)+1;
         int col = generator.nextInt(boardSize)+1;

         if(board[row][col]==0)
            board[row][col] = 1;
         else
            i--;
      }

      int row = boardSize/2;
      int col = boardSize/2;

      if(board[row][col]==0)
      {
         currLocation.row = row;
         currLocation.col = col;
         currLocation.val = 2;
      }
      else
      {
         while(board[row][col]!=0)
         {
            row = generator.nextInt(boardSize)+1;
            col = generator.nextInt(boardSize)+1;
         }

         currLocation.row = row;
         currLocation.col = col;
         currLocation.val = 2;
      }

      board[row][col] = 2;
   }


   private static void undoMove()
   {
      if(!userMove.empty())
      {
         Location temp = userMove.pop();

         board[temp.row][temp.col] = 0;

         boardObject[temp.row][temp.col].setMark(0);
      }
      if(!ratMove.empty())
      {
         board[currLocation.row][currLocation.col] = 0;
         boardObject[currLocation.row][currLocation.col].setMark(0);
         currLocation = ratMove.pop();
         board[currLocation.row][currLocation.col] = 2;
         boardObject[currLocation.row][currLocation.col].setMark(2);
      }
   }


   public void addComponentToThread(Square changeThisSquare)
   {
      SwingUtilities.invokeLater(
            new Runnable()
            {
               public void run()
               {
                  try{
                  Thread.sleep(generator.nextInt(5));
                  changeThisSquare.componentChanged();}
                  catch(InterruptedException e)
                  {
                     e.printStackTrace();
                  }
               }
            }
            );
   }


   private class Square extends JPanel
   {
      private int color;
      private int location;

      public Square(int color, int location)
      {
         this.color = color;
         this.location = location;

         addMouseListener(
               new MouseAdapter()
               {
                  public void mouseReleased(MouseEvent e)
                  {
                     moveUser(location);
                  }
               }
               );
      }

      public Dimension getPreferredSize()
      {
         return new Dimension(50, 50);
      }

      public Dimension getMinimumSize()
      {
         return getPreferredSize();
      }

      public void paintComponent(Graphics g)
      {
         super.paintComponent(g);
         g.setColor(Color.BLACK);
         g.drawRect(0,0,49,49);

         switch(color)
         {
            case 1: g.setColor(Color.RED);
                    g.fillRect(0,0,49,49);
                    break;
            case -1: g.setColor(Color.BLUE);
                     g.fillRect(0,0,49,49);
                     break;
            case 2: g.setColor(Color.GREEN);
                    g.fillRect(0,0,49,49);
                    break;
            case 0: g.setColor(Color.WHITE);
                    g.fillRect(0,0,49,49);
                    break;
         }
      }

      public void moveUser(int loc)
      {
         int row = loc/(boardSize+2);
         int col = loc - (row * (boardSize+2));

         if(board[row][col]==0)
         {
            board[row][col] = 1;
            color = 1;
            addComponentToThread(Square.this);

            userMove.push(new Location(row,col,0));

            Location temp = moveRat(currLocation);

            board[currLocation.row][currLocation.col] = 0;
            boardObject[currLocation.row][currLocation.col].setMark(0);
            board[temp.row][temp.col] = 2;
            boardObject[temp.row][temp.col].setMark(2);
            if(temp.row == 0)
            {
               undoButton.setEnabled(false);
               JOptionPane.showMessageDialog(null,"YOU WON!!!!!");
            }
            ratMove.push(currLocation);
            currLocation = temp;
            currLocation.val = 2;

            for(int i = 1; i <= boardSize; i++)
            {
               for(int j = 1; j <= boardSize; j++)
               {
                  if(board[i][j]==1||board[i][j]==-1||board[i][j]==2)
                     continue;
                  board[i][j] = 0;
               }
            }


            if(gameOver())
            {
               undoButton.setEnabled(false);
               JOptionPane.showMessageDialog(null,"YOU LOSE!!!");
            }
         }
         else
         {
            JOptionPane.showMessageDialog(null,"Invalid Move");
         }
      }

      public Location moveRat(Location curr)
      {
         return nextLocation(markPath(curr),curr);
      }

      public Location markPath(Location curr)
      {
         tempBoard = board;
         Location tempLoc = null;
         Queue<Location> queue = new LinkedList<Location>();
         queue.offer(curr);
         Location max = curr;
         while(queue.size()>0)
         {
            tempLoc = queue.poll();
            for(int i = 0; i < 8; i++)
            {
               if(board[tempLoc.row + dir[i][0]][tempLoc.col + dir[i][1]] == 0 ||
                   board[tempLoc.row + dir[i][0]][tempLoc.col + dir[i][1]] == -1 )
               {
                  queue.offer(new Location(tempLoc.row+dir[i][0],tempLoc.col+dir[i][1],
                           tempLoc.val+1));
                  tempBoard[tempLoc.row + dir[i][0]][tempLoc.col + dir[i][1]] = tempLoc.val+1;
                  if(max.val < tempLoc.val +1)
                     max = queue.peek();
               }
            }
         }

         Location min = new Location(0,0,Integer.MAX_VALUE);

         for(Location loc : exits)
           {
              if(tempBoard[loc.row][loc.col]>0)
              {
                 if(tempBoard[loc.row][loc.col]<min.val)
                   {
                      min = loc;
                      min.val = tempBoard[loc.row][loc.col];
                   }
              }
           }


         if(min.val == Integer.MAX_VALUE)
         {
            return max;
         }

         return  min;

      }

      public Location nextLocation(Location temp, Location curr)
      {
         Stack<Location> stack = new Stack<Location>();
         Location temp1;
         stack.push(temp);
         while(stack.peek().row != curr.row || stack.peek().col != curr.col)
         {
            temp1 = stack.peek();
            for(int i = 0; i < 8; i++)
            {
               if(tempBoard[temp1.row + dir[i][0]][temp1.col + dir[i][1]] != 1 &&
                  tempBoard[temp1.row + dir[i][0]][temp1.col + dir[i][1]] < tempBoard[temp1.row][temp1.col])
               {
                  stack.push(new Location(temp1.row + dir[i][0], temp1.col + dir[i][1],
                           tempBoard[temp1.row + dir[i][0]][temp1.col + dir[i][1]]));
                  break;
               }
            }
         }

         if(stack.size()==0)
            return new Location(0,0,0);
         else
         {
            stack.pop();
            return stack.pop();
         }
      }

      public void setMark(int color)
      {
         this.color = color;
         addComponentToThread(Square.this);
      }

      public void componentChanged()
      {
         repaint();
      }

      public boolean gameOver()
      {
         for(Location loc : exits)
         {
            if(loc.row == currLocation.row && loc.col == currLocation.col)
               return true;
         }
         return false;
      }
   }

   public static void main(String[] args)
   {
      SwingUtilities.invokeLater(
            new Runnable()
            {
               public void run()
               {

                  JDialog.setDefaultLookAndFeelDecorated(true);

                  CatchTheRat app;

                   if(args.length==0)
                     app = new CatchTheRat(10);
                   else
                     {
                        if(Integer.parseInt(args[0])>=6 && Integer.parseInt(args[0])<=12)
                           app = new CatchTheRat(Integer.parseInt(args[0]));
                        else
                           app = new CatchTheRat(10);
                      }
                  app.setMinimumSize(new Dimension(700,700));
                  app.setExtendedState(MAXIMIZED_BOTH);
                  app.pack();
                  app.setDefaultCloseOperation(EXIT_ON_CLOSE);
               }
            }
            );
   }
}

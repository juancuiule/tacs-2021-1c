import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import LinkButton from '../../src/LinkButton';
import Link from '../../src/Link';
import { useSession, signOut } from 'next-auth/client';

const useStyles = makeStyles(theme => ({
  appBar: {
    borderBottom: `1px solid ${theme.palette.divider}`,
  },
  toolbar: {
    flexWrap: 'wrap',
  },
  toolbarTitle: {
    flexGrow: 1,
  },
  link: {
    margin: theme.spacing(1, 1.5),
  },
}));

const Navbar = () => {
  const classes = useStyles();
  const [session, loading] = useSession();

  return (
    <AppBar
      position="static"
      color="default"
      elevation={0}
      className={classes.appBar}
    >
      <Toolbar className={classes.toolbar}>
        <Typography
          variant="h6"
          color="inherit"
          noWrap
          className={classes.toolbarTitle}
        >
          Super Amigos
        </Typography>

        {session && (
          <nav>
            <Link
              variant="button"
              color="textPrimary"
              href="/cards"
              className={classes.link}
            >
              Cards
            </Link>
            <Link
              variant="button"
              color="textPrimary"
              href="/decks"
              className={classes.link}
            >
              Decks
            </Link>
          </nav>
        )}

        {!session && (
          <LinkButton
            href="/auth/signin"
            color="primary"
            variant="outlined"
            className={classes.link}
          >
            Sign In
          </LinkButton>
        )}

        {session && (
          <LinkButton
            href="/auth/signin"
            color="primary"
            variant="outlined"
            className={classes.link}
            onClick={signOut}
          >
            LogOut
          </LinkButton>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;

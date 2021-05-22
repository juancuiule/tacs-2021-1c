import * as React from 'react'
import {
  default as MUITextField,
  StandardTextFieldProps,
} from '@material-ui/core/TextField'
import { makeStyles } from '@material-ui/styles'

const useStyles = makeStyles({
  root: {
    marginTop: '16px',
    marginBottom: '10px',
  },
  rootWithHandleError: {
    marginTop: '24px',
    marginBottom: '24px',
  },
  input: {
    fontFamily: 'Gotham',
    fontWeight: 400,
    fontSize: 18,
    '@media only screen and (max-width : 800px)': {
      fontSize: 15,
    },
    '&:before': {
      borderBottomColor: 'var(--gray-dark) !important',
    },
    '&:hover:before': {
      borderBottomColor: 'var(--gray-dark) !important',
    },
  },
  cssUnderline: {
    '&:after': {
      borderBottomColor: 'var(--primary)',
    },
  },
  label: {
    fontFamily: 'Gotham',
    fontWeight: 400,
  },
  labelFocused: {
    color: 'var(--primary) !important',
  },

  errorLabel: {
    color: 'var(--accent) !important',
  },
  errorCssUnderline: {
    '&:after': {
      borderBottomColor: 'var(--accent)',
    },
  },
  errorMessage: {
    color: 'var(--accent)',
    float: 'left',
    fontSize: '12px',
    textAlign: 'left',
    marginTop: '-20px',
  },
})

interface ExtraProps {
  errorMessage?: string
  handleError?: boolean
}

const TextField = ({
  fullWidth = true,
  handleError = false,
  errorMessage,
  InputProps,
  children,
  ...rest
}: StandardTextFieldProps & ExtraProps) => {
  const classes = useStyles()
  return (
    <>
      <MUITextField
        fullWidth={fullWidth}
        className={handleError ? classes.rootWithHandleError : classes.root}
        InputProps={{
          ...InputProps,
          classes: {
            root: classes.input,
            underline: rest.error
              ? classes.errorCssUnderline
              : classes.cssUnderline,
            ...(InputProps ? InputProps.classes : {}),
          },
        }}
        InputLabelProps={{
          classes: {
            root: classes.label,
            focused: rest.error ? classes.errorLabel : classes.labelFocused,
          },
        }}
        {...rest}
      />
      {handleError ? (
        <span className={classes.errorMessage}>
          {rest.error ? errorMessage : ''}
        </span>
      ) : null}
    </>
  )
}

export default TextField

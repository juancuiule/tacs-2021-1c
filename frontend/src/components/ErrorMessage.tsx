import React from 'react'
import { makeStyles } from '@material-ui/styles'

const useStyles = makeStyles({
  errorContainer: {
    width: '100%',
    fontSize: 14,
    color: 'var(--accent)',
    textAlign: 'left',
    borderLeft: '4px solid var(--accent)',
    marginTop: '10px',
    paddingLeft: '10px',
    '& > p': {
      margin: 0,
      padding: 0,
    },
  },
})

interface Props {
  condition?: boolean
  message?: string
  style?: React.CSSProperties
  className?: string
}

const ErrorMessage = (props: Props) => {
  const { condition, message } = props
  const classes = useStyles()
  return condition ? (
    <div
      className={`${classes.errorContainer} ${props.className}`}
      style={{ ...props.style }}
    >
      <p>{message || 'Esta pregunta es obligatoria.'}</p>
    </div>
  ) : null
}

export default ErrorMessage
